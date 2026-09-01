package com.kerflowapp.kerflow.services.search;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kerflowapp.kerflow.api.search.domain.SearchRequest;
import com.kerflowapp.kerflow.api.search.domain.SearchResponse;
import com.kerflowapp.kerflow.api.search.domain.SearchResultDto;
import com.kerflowapp.kerflow.mappers.ProspectMapper;
import com.kerflowapp.kerflow.services.enrichment.EnrichmentContext;
import com.kerflowapp.kerflow.services.enrichment.EnrichmentEngine;
import com.kerflowapp.kerflow.services.enrichment.EnrichmentMode;
import com.kerflowapp.kerflow.services.scoring.ProspectScoringService;
import com.kerflowapp.kerflow.services.search.WebsiteScraperService.SocialLinks;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class GooglePlacesService {

    private static final String TEXT_SEARCH_URL = "https://maps.googleapis.com/maps/api/place/textsearch/json";
    private static final String DETAILS_URL = "https://maps.googleapis.com/maps/api/place/details/json";
    // editorial_summary belongs to the same billed SKU as reviews, so it costs nothing extra.
    private static final String DETAILS_FIELDS = String.join(",",
        "name", "formatted_address", "formatted_phone_number", "website", "url",
        "rating", "user_ratings_total", "geometry", "types", "business_status",
        "opening_hours", "reviews", "editorial_summary"
    );
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);

    private final String apiKey;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final ProspectScoringService prospectScoringService;
    private final EnrichmentEngine enrichmentEngine;
    private final ProspectMapper prospectMapper;
    private final ExecutorService scraperExecutor;

    public GooglePlacesService(
        @Value("${google.places.api-key:}") String apiKey,
        ObjectMapper objectMapper,
        ProspectScoringService prospectScoringService,
        EnrichmentEngine enrichmentEngine,
        ProspectMapper prospectMapper) {
        this.apiKey = apiKey;
        this.objectMapper = objectMapper;
        this.prospectScoringService = prospectScoringService;
        this.enrichmentEngine = enrichmentEngine;
        this.prospectMapper = prospectMapper;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
        this.scraperExecutor = Executors.newFixedThreadPool(10);
    }

    public SearchResponse search(SearchRequest request) {
        boolean hasCoordinates = request.lat() != null && request.lng() != null;
        // When coordinates are provided (always the case from the frontend via Nominatim),
        // use the keywords only and rely on location + radius for the geographic bias.
        // Appending the city to the query makes Google match the city name textually and
        // drastically reduces the number of results. Fall back to "keywords city" only when
        // no coordinates are available.
        String query = hasCoordinates
            ? request.keywords()
            : request.keywords() + " " + request.city();

        StringBuilder urlBuilder = new StringBuilder(TEXT_SEARCH_URL)
            .append("?query=").append(URLEncoder.encode(query, StandardCharsets.UTF_8))
            .append("&key=").append(apiKey)
            .append("&language=fr");

        if (hasCoordinates) {
            urlBuilder.append("&location=").append(request.lat()).append(",").append(request.lng());
            if (request.radius() != null) {
                // The frontend sends the radius in kilometers; Google Places expects meters (max 50000).
                int radiusMeters = Math.min(request.radius() * 1000, 50000);
                urlBuilder.append("&radius=").append(radiusMeters);
            }
        }

        if (request.pageToken() != null && !request.pageToken().isBlank()) {
            urlBuilder.append("&pagetoken=").append(URLEncoder.encode(request.pageToken(), StandardCharsets.UTF_8));
        }

        try {
            HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(urlBuilder.toString()))
                .timeout(REQUEST_TIMEOUT)
                .GET()
                .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                LOGGER.error("Google Places text search failed: {} {}", response.statusCode(), response.body());
                return SearchResponse.builder().results(List.of()).build();
            }

            JsonNode root = objectMapper.readTree(response.body());
            List<SearchResultDto> results = new ArrayList<>();

            JsonNode resultsNode = root.path("results");
            for (JsonNode place : resultsNode) {
                String placeId = place.path("place_id").asText(null);
                SearchResultDto detail = fetchPlaceDetails(placeId, place);
                if (detail != null) {
                    results.add(detail);
                }
            }

            List<SearchResultDto> enriched = enrichResults(results);
            List<SearchResultDto> scored = prospectScoringService.scoreAndSort(enriched);

            String nextPageToken = root.has("next_page_token")
                ? root.path("next_page_token").asText(null)
                : null;

            return SearchResponse.builder()
                .results(scored)
                .nextPageToken(nextPageToken)
                .build();

        } catch (Exception e) {
            LOGGER.error("Error performing Google Places search", e);
            return SearchResponse.builder().results(List.of()).build();
        }
    }

    /**
     * Runs the enrichment engine on every result (website scrape included, so it stays
     * on the async pool) and copies back social links + detected signals.
     */
    private List<SearchResultDto> enrichResults(List<SearchResultDto> results) {
        List<CompletableFuture<SearchResultDto>> futures = results.stream()
            .map(result -> CompletableFuture.supplyAsync(() -> enrichResult(result), scraperExecutor))
            .toList();

        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .get(15, TimeUnit.SECONDS);
        } catch (Exception e) {
            LOGGER.warn("Timeout waiting for result enrichment", e);
        }

        return futures.stream()
            .map(f -> f.getNow(null))
            .filter(Objects::nonNull)
            .toList();
    }

    private SearchResultDto enrichResult(SearchResultDto result) {
        EnrichmentContext context = EnrichmentContext.builder()
            .mode(EnrichmentMode.SEARCH)
            .name(result.name())
            .address(result.address())
            .phone(result.phone())
            .website(result.website())
            .googlePlaceId(result.placeId())
            .rating(result.rating())
            .userRatingsTotal(result.userRatingsTotal())
            .googleTypes(result.types() != null ? result.types() : List.of())
            .build();
        enrichmentEngine.run(context);

        SearchResultDto.SearchResultDtoBuilder builder = result.toBuilder()
            .signals(prospectMapper.toSignalDtos(context.getSignals()));

        if (context.getWebsiteAudit() != null && context.getWebsiteAudit().socialLinks() != null) {
            SocialLinks links = context.getWebsiteAudit().socialLinks();
            builder.instagram(links.instagram())
                .facebook(links.facebook())
                .linkedin(links.linkedin())
                .twitter(links.twitter())
                .tiktok(links.tiktok())
                .youtube(links.youtube());
        }
        return builder.build();
    }

    private SearchResultDto fetchPlaceDetails(String placeId, JsonNode textSearchResult) {
        if (placeId == null) return null;

        try {
            String url = DETAILS_URL
                + "?place_id=" + URLEncoder.encode(placeId, StandardCharsets.UTF_8)
                + "&fields=" + DETAILS_FIELDS
                + "&key=" + apiKey
                + "&language=fr";

            HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(REQUEST_TIMEOUT)
                .GET()
                .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                LOGGER.warn("Failed to fetch details for place {}: {}", placeId, response.statusCode());
                return buildResultFromTextSearch(placeId, textSearchResult);
            }

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode result = root.path("result");

            if (result.isMissingNode()) {
                return buildResultFromTextSearch(placeId, textSearchResult);
            }

            JsonNode geometry = result.path("geometry").path("location");
            Double lat = geometry.has("lat") ? geometry.path("lat").asDouble() : null;
            Double lng = geometry.has("lng") ? geometry.path("lng").asDouble() : null;

            List<String> types = new ArrayList<>();
            for (JsonNode type : result.path("types")) {
                types.add(type.asText());
            }

            SearchResultDto.OpeningHours openingHours = parseOpeningHours(result);
            List<SearchResultDto.Review> reviews = parseReviews(result);

            return SearchResultDto.builder()
                .placeId(placeId)
                .name(result.path("name").asText(null))
                .address(result.path("formatted_address").asText(null))
                .phone(result.path("formatted_phone_number").asText(null))
                .website(result.path("website").asText(null))
                .googleMapsUrl(result.path("url").asText(null))
                .rating(result.has("rating") ? result.path("rating").asDouble() : null)
                .userRatingsTotal(result.has("user_ratings_total") ? result.path("user_ratings_total").asInt() : null)
                .lat(lat)
                .lng(lng)
                .types(types)
                .businessStatus(result.path("business_status").asText(null))
                .openingHours(openingHours)
                .reviews(reviews)
                .editorialSummary(result.path("editorial_summary").path("overview").asText(null))
                .build();

        } catch (Exception e) {
            LOGGER.warn("Error fetching details for place {}", placeId, e);
            return buildResultFromTextSearch(placeId, textSearchResult);
        }
    }

    private SearchResultDto.OpeningHours parseOpeningHours(JsonNode result) {
        if (!result.has("opening_hours")) return null;

        JsonNode oh = result.path("opening_hours");
        List<String> weekdayText = new ArrayList<>();
        for (JsonNode wt : oh.path("weekday_text")) {
            weekdayText.add(wt.asText());
        }
        return SearchResultDto.OpeningHours.builder()
            .openNow(oh.path("open_now").asBoolean(false))
            .weekdayText(weekdayText)
            .build();
    }

    private List<SearchResultDto.Review> parseReviews(JsonNode result) {
        if (!result.has("reviews")) return List.of();

        List<SearchResultDto.Review> reviews = new ArrayList<>();
        for (JsonNode reviewNode : result.path("reviews")) {
            reviews.add(SearchResultDto.Review.builder()
                .authorName(reviewNode.path("author_name").asText(null))
                .profilePhotoUrl(reviewNode.path("profile_photo_url").asText(null))
                .rating(reviewNode.path("rating").asInt(0))
                .text(reviewNode.path("text").asText(null))
                .relativeTimeDescription(reviewNode.path("relative_time_description").asText(null))
                .build());
        }
        return reviews;
    }

    private SearchResultDto buildResultFromTextSearch(String placeId, JsonNode place) {
        JsonNode geometry = place.path("geometry").path("location");

        List<String> types = new ArrayList<>();
        for (JsonNode type : place.path("types")) {
            types.add(type.asText());
        }

        return SearchResultDto.builder()
            .placeId(placeId)
            .name(place.path("name").asText(null))
            .address(place.path("formatted_address").asText(null))
            .rating(place.has("rating") ? place.path("rating").asDouble() : null)
            .userRatingsTotal(place.has("user_ratings_total") ? place.path("user_ratings_total").asInt() : null)
            .lat(geometry.has("lat") ? geometry.path("lat").asDouble() : null)
            .lng(geometry.has("lng") ? geometry.path("lng").asDouble() : null)
            .types(types)
            .businessStatus(place.path("business_status").asText(null))
            .build();
    }
}
