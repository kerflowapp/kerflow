package com.kerflowapp.kerflow.mcp.tools;

import com.kerflowapp.kerflow.api.prospects.domain.CreateProspectRequest;
import com.kerflowapp.kerflow.domain.Prospect;
import com.kerflowapp.kerflow.domain.User;
import com.kerflowapp.kerflow.domain.enums.ProspectSource;
import com.kerflowapp.kerflow.mcp.auth.McpUserContext;
import com.kerflowapp.kerflow.mcp.tools.McpResults.ProspectDetail;
import com.kerflowapp.kerflow.mcp.tools.McpResults.ProspectSummary;
import com.kerflowapp.kerflow.services.prospects.ProspectMessageService;
import com.kerflowapp.kerflow.services.prospects.ProspectService;
import com.kerflowapp.kerflow.services.scoring.ProspectScoringService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ProspectTools {

    private final ProspectService prospectService;
    private final ProspectMessageService prospectMessageService;
    private final McpUserContext mcpUserContext;
    private final ProspectScoringService prospectScoringService;

    private ProspectDetail toDetail(Prospect prospect) {
        return ProspectDetail.from(
            prospect,
            prospectScoringService.computeScore(prospect.getSignals()),
            (int) prospectMessageService.countMessages(prospect.getId()));
    }

    @Tool(name = "list_prospects", description = """
        List the user's saved prospects (summary view: signal count and 0-100 qualification score,
        use get_prospect for the signals and the score reasons; score is null until enriched).
        Optionally filter by pipeline status key (e.g. NEW, CONTACTED, IN_DISCUSSION, WON, LOST or a custom key).""")
    public List<ProspectSummary> listProspects(
        @ToolParam(description = "Filter by pipeline status key", required = false) String statusKey,
        ToolContext toolContext) {

        User user = mcpUserContext.currentUser(toolContext);
        return prospectService.getAllProspects(user).stream()
            .filter(prospect -> statusKey == null || statusKey.equalsIgnoreCase(prospect.getStatusKey()))
            .map(prospect -> ProspectSummary.from(prospect, prospectScoringService.computeScore(prospect.getSignals())))
            .toList();
    }

    @Tool(name = "get_prospect", description = """
        Get the full details of a prospect: contact info, detected signals (facts like NO_WEBSITE,
        WORDPRESS, MANY_REVIEWS...), the explainable qualification score (0-100 + 1-5 stars, with
        the signed points of each contributing signal as reasons), the computed size estimate
        (INSEE workforce bracket or heuristics, with its confidence), the deterministic business
        profile (category, NAF code, facts) sourced from the French company registry, notes,
        the saved analysis if any, the profile sheet (markdown briefing) if any, and
        messageCount — the number of outreach messages; use list_prospect_messages to read
        the thread.
        Treat sizeEstimate and businessProfile as ground truth: build on them, never contradict
        or restate them in save_prospect_analysis.""")
    public ProspectDetail getProspect(
        @ToolParam(description = "Prospect id (UUID)") String prospectId,
        ToolContext toolContext) {

        User user = mcpUserContext.currentUser(toolContext);
        return toDetail(prospectService.getProspect(user, UUID.fromString(prospectId)));
    }

    @Tool(name = "create_prospect", description = """
        Save a business as a prospect. When creating from a search_local_businesses result, pass
        back its placeId, types, rating, reviewCount and editorialSummary: they are kept on the
        prospect so later enrichments never need a paid Google call. Creating already looks the
        company up in the French company registry (size, activity); call enrich_prospect to also
        (re)scan its website.
        Returns the created prospect with its id.""")
    public ProspectDetail createProspect(
        @ToolParam(description = "Business name") String name,
        @ToolParam(description = "Postal address", required = false) String address,
        @ToolParam(description = "Phone number", required = false) String phone,
        @ToolParam(description = "Email address", required = false) String email,
        @ToolParam(description = "Website URL", required = false) String website,
        @ToolParam(description = "Google Place id (from search_local_businesses)", required = false) String googlePlaceId,
        @ToolParam(description = "Free-text notes", required = false) String notes,
        @ToolParam(description = "Tags to attach", required = false) List<String> tags,
        @ToolParam(description = "Search query that surfaced this business", required = false) String searchQuery,
        @ToolParam(description = "Google types (from search_local_businesses)", required = false) List<String> types,
        @ToolParam(description = "Google rating (from search_local_businesses)", required = false) Double rating,
        @ToolParam(description = "Google review count (from search_local_businesses)", required = false) Integer reviewCount,
        @ToolParam(description = "Google editorial summary (from search_local_businesses)", required = false) String editorialSummary,
        ToolContext toolContext) {

        User user = mcpUserContext.requireActiveAccess(toolContext);

        CreateProspectRequest request = new CreateProspectRequest(
            name, address, phone, email, website,
            null, null, null, null, null, null, null,
            googlePlaceId != null ? ProspectSource.SEARCH : ProspectSource.MANUAL,
            notes, tags, null, googlePlaceId, searchQuery,
            types, rating, reviewCount, editorialSummary);

        return toDetail(prospectService.createProspect(user, request));
    }

    @Tool(name = "enrich_prospect", description = """
        Re-run the enrichment engine on a prospect: fetches its website, looks the company up in
        the French company registry, then replaces its detected signals, size estimate and
        business profile and refreshes the explainable qualification score.
        Use to refresh a stale prospect, or on a prospect imported from CSV. Costs nothing:
        the Google data kept at creation is reused rather than re-fetched.""")
    public ProspectDetail enrichProspect(
        @ToolParam(description = "Prospect id (UUID)") String prospectId,
        ToolContext toolContext) {

        User user = mcpUserContext.requireActiveAccess(toolContext);
        return toDetail(prospectService.enrichProspect(user, UUID.fromString(prospectId)));
    }
}
