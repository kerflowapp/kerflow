package com.kerflowapp.kerflow.services.search;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class WebsiteScraperService {

    private static final int TIMEOUT_MS = 5_000;
    private static final String USER_AGENT = "Mozilla/5.0 (compatible; Kerflow/1.0)";

    private static final Map<String, Pattern> SOCIAL_PATTERNS = Map.of(
        "facebook", Pattern.compile("https?://(www\\.)?(facebook\\.com|fb\\.com)/[^\"'\\s#?]+", Pattern.CASE_INSENSITIVE),
        "instagram", Pattern.compile("https?://(www\\.)?instagram\\.com/[^\"'\\s#?]+", Pattern.CASE_INSENSITIVE),
        "linkedin", Pattern.compile("https?://(www\\.)?linkedin\\.com/(company|in)/[^\"'\\s#?]+", Pattern.CASE_INSENSITIVE),
        "twitter", Pattern.compile("https?://(www\\.)?(twitter\\.com|x\\.com)/[^\"'\\s#?]+", Pattern.CASE_INSENSITIVE),
        "tiktok", Pattern.compile("https?://(www\\.)?tiktok\\.com/@[^\"'\\s#?]+", Pattern.CASE_INSENSITIVE),
        "youtube", Pattern.compile("https?://(www\\.)?youtube\\.com/(channel|c|@)[^\"'\\s#?]+", Pattern.CASE_INSENSITIVE)
    );

    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}");

    // Keyword lists live as constants for the MVP; candidates for per-business-type config in Phase 7.
    // Matched against accent-stripped lowercase text (see normalize()).
    private static final List<String> RECRUITING_KEYWORDS = List.of(
        "recrutement", "nous rejoindre", "carrieres", "on recrute", "offres d'emploi", "rejoignez-nous"
    );
    private static final List<String> FLEET_KEYWORDS = List.of(
        "notre flotte", "nos vehicules", "notre parc", "nos camions", "nos utilitaires"
    );

    @Builder
    public record SocialLinks(
        String facebook,
        String instagram,
        String linkedin,
        String twitter,
        String tiktok,
        String youtube
    ) {
        
        public static SocialLinks empty() {
            return SocialLinks.builder().build();
        }

    }

    @Builder
    public record WebsiteAudit(
        SocialLinks socialLinks,
        String finalUrl,
        boolean https,
        boolean wordpress,
        List<String> emails,
        boolean recruitingHints,
        boolean fleetHints
    ) {
        public static WebsiteAudit empty() {
            return WebsiteAudit.builder()
                .socialLinks(SocialLinks.empty())
                .emails(List.of())
                .build();
        }
    }

    public WebsiteAudit scrape(String websiteUrl) {
        if (websiteUrl == null || websiteUrl.isBlank()) {
            return WebsiteAudit.empty();
        }

        try {
            Document doc = Jsoup.connect(websiteUrl)
                .userAgent(USER_AGENT)
                .timeout(TIMEOUT_MS)
                .followRedirects(true)
                .get();

            SocialLinks.SocialLinksBuilder socialBuilder = SocialLinks.builder();
            Set<String> emails = new LinkedHashSet<>();
            StringBuilder anchorContent = new StringBuilder();

            for (Element link : doc.select("a[href]")) {
                String rawHref = link.attr("href");
                if (rawHref.toLowerCase(Locale.ROOT).startsWith("mailto:")) {
                    extractEmail(rawHref.substring("mailto:".length()), emails);
                }
                anchorContent.append(' ').append(link.text()).append(' ').append(rawHref);

                String href = link.absUrl("href");
                if (href.isEmpty()) continue;

                for (Map.Entry<String, Pattern> entry : SOCIAL_PATTERNS.entrySet()) {
                    if (entry.getValue().matcher(href).matches()) {
                        switch (entry.getKey()) {
                            case "facebook" -> socialBuilder.facebook(cleanUrl(href));
                            case "instagram" -> socialBuilder.instagram(cleanUrl(href));
                            case "linkedin" -> socialBuilder.linkedin(cleanUrl(href));
                            case "twitter" -> socialBuilder.twitter(cleanUrl(href));
                            case "tiktok" -> socialBuilder.tiktok(cleanUrl(href));
                            case "youtube" -> socialBuilder.youtube(cleanUrl(href));
                        }
                    }
                }
            }

            String pageText = doc.text();
            Matcher emailMatcher = EMAIL_PATTERN.matcher(pageText);
            while (emailMatcher.find()) {
                extractEmail(emailMatcher.group(), emails);
            }

            String finalUrl = doc.location();
            String normalizedContent = normalize(pageText + anchorContent);

            return WebsiteAudit.builder()
                .socialLinks(socialBuilder.build())
                .finalUrl(finalUrl)
                .https(finalUrl != null && finalUrl.toLowerCase(Locale.ROOT).startsWith("https://"))
                .wordpress(detectWordpress(doc))
                .emails(new ArrayList<>(emails))
                .recruitingHints(containsAny(normalizedContent, RECRUITING_KEYWORDS))
                .fleetHints(containsAny(normalizedContent, FLEET_KEYWORDS))
                .build();
        } catch (Exception e) {
            LOGGER.debug("Failed to scrape website {}: {}", websiteUrl, e.getMessage());
            return WebsiteAudit.empty();
        }
    }

    private boolean detectWordpress(Document doc) {
        Element generator = doc.selectFirst("meta[name=generator]");
        if (generator != null && generator.attr("content").toLowerCase(Locale.ROOT).contains("wordpress")) {
            return true;
        }
        if (doc.selectFirst("link[href*=wp-json]") != null) {
            return true;
        }
        return doc.html().contains("/wp-content/");
    }

    private void extractEmail(String candidate, Set<String> emails) {
        // Strip mailto query params ("?subject=...") before validating.
        int queryIdx = candidate.indexOf('?');
        if (queryIdx >= 0) {
            candidate = candidate.substring(0, queryIdx);
        }
        candidate = candidate.trim().toLowerCase(Locale.ROOT);
        if (EMAIL_PATTERN.matcher(candidate).matches()) {
            emails.add(candidate);
        }
    }

    private boolean containsAny(String normalizedContent, List<String> keywords) {
        return keywords.stream().anyMatch(normalizedContent::contains);
    }

    private String normalize(String text) {
        String lowered = text.toLowerCase(Locale.ROOT).replace('’', '\'');
        return Normalizer.normalize(lowered, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
    }

    private String cleanUrl(String url) {
        int queryIdx = url.indexOf('?');
        if (queryIdx > 0) {
            url = url.substring(0, queryIdx);
        }
        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        return url;
    }
}
