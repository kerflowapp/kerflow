package com.kerflowapp.kerflow.mcp.tools;

import com.kerflowapp.kerflow.domain.Prospect;
import com.kerflowapp.kerflow.domain.ProspectAnalysis;
import com.kerflowapp.kerflow.domain.User;
import com.kerflowapp.kerflow.mcp.auth.McpUserContext;
import com.kerflowapp.kerflow.mcp.tools.McpResults.ProspectDetail;
import com.kerflowapp.kerflow.services.prospects.ProspectMessageService;
import com.kerflowapp.kerflow.services.prospects.ProspectService;
import com.kerflowapp.kerflow.services.scoring.ProspectScoringService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AnalysisTools {

    private static final String GENERATED_BY = "mcp-agent";

    private final ProspectService prospectService;
    private final ProspectMessageService prospectMessageService;
    private final McpUserContext mcpUserContext;
    private final ProspectScoringService prospectScoringService;

    @Tool(name = "save_prospect_analysis", description = """
        Persist your qualification analysis of a prospect so it is visible in the Kerflow web app
        and in future conversations. Call get_prospect first and reason over its signals, score,
        size estimate and business profile. Overwrites any previous analysis.
        Do not restate the deterministic business profile (category, NAF code, company age, size):
        it is already computed and displayed next to your analysis. Add what it cannot compute —
        specialisation, positioning, buying context, objections.""")
    public ProspectDetail saveProspectAnalysis(
        @ToolParam(description = "Prospect id (UUID)") String prospectId,
        @ToolParam(description = "Short qualification summary of the business (2-4 sentences)") String summary,
        @ToolParam(description = "Detected needs / opportunities, each with a short justification", required = false) List<String> detectedNeeds,
        @ToolParam(description = "Suggested outreach approach for this prospect", required = false) String suggestedApproach,
        ToolContext toolContext) {

        User user = mcpUserContext.requireActiveAccess(toolContext);

        ProspectAnalysis analysis = new ProspectAnalysis(
            summary, detectedNeeds, suggestedApproach, GENERATED_BY, Instant.now());

        Prospect prospect = prospectService.saveAnalysis(user, UUID.fromString(prospectId), analysis);
        return ProspectDetail.from(
            prospect,
            prospectScoringService.computeScore(prospect.getSignals()),
            (int) prospectMessageService.countMessages(prospect.getId()));
    }

}
