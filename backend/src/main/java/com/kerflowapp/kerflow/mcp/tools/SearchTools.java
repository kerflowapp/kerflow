package com.kerflowapp.kerflow.mcp.tools;

import com.kerflowapp.kerflow.api.search.domain.SearchRequest;
import com.kerflowapp.kerflow.api.search.domain.SearchResponse;
import com.kerflowapp.kerflow.mcp.auth.McpUserContext;
import com.kerflowapp.kerflow.mcp.tools.McpResults.BusinessResult;
import com.kerflowapp.kerflow.mcp.tools.McpResults.BusinessSearchResult;
import com.kerflowapp.kerflow.services.search.GooglePlacesService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SearchTools {

    private final GooglePlacesService googlePlacesService;
    private final McpUserContext mcpUserContext;

    @Tool(name = "search_local_businesses", description = """
        Search Google Places for local businesses matching a keyword and a city.
        Each result includes prospecting signals (e.g. NO_WEBSITE, PROFESSIONAL_WEBSITE, MANY_REVIEWS,
        RECRUITING_DETECTED, FLEET_DETECTED, PRO_EMAIL...) and an explainable qualification score
        (0-100 + 1-5 stars, with the signed points of each contributing signal as reasons).
        Use the returned placeId with create_prospect to save a result as a prospect.
        Pass nextPageToken from a previous response to fetch the next page.""")
    public BusinessSearchResult searchLocalBusinesses(
        @ToolParam(description = "Business type keywords, e.g. 'ambulance', 'taxi', 'restaurant'") String keywords,
        @ToolParam(description = "City name, e.g. 'Lyon'") String city,
        @ToolParam(description = "Search radius in kilometers", required = false) Integer radiusKm,
        @ToolParam(description = "Page token from a previous search to get the next page", required = false) String pageToken,
        ToolContext toolContext) {

        mcpUserContext.requireActiveAccess(toolContext);

        SearchResponse response = googlePlacesService.search(
            new SearchRequest(keywords, city, radiusKm, null, null, pageToken));

        return new BusinessSearchResult(
            response.results().stream().map(BusinessResult::from).toList(),
            response.nextPageToken());
    }
}
