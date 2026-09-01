package com.kerflowapp.kerflow.mcp.tools;

import com.kerflowapp.kerflow.domain.ProfileSheet;
import com.kerflowapp.kerflow.domain.Prospect;
import com.kerflowapp.kerflow.domain.ProspectMessage;
import com.kerflowapp.kerflow.domain.User;
import com.kerflowapp.kerflow.mcp.auth.McpUserContext;
import com.kerflowapp.kerflow.mcp.tools.McpResults.MessageResult;
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
public class OutreachTools {

    private static final String GENERATED_BY = "mcp-agent";

    private final ProspectService prospectService;
    private final ProspectMessageService prospectMessageService;
    private final McpUserContext mcpUserContext;
    private final ProspectScoringService prospectScoringService;

    @Tool(name = "save_prospect_email", description = """
        Save a cold-outreach or follow-up email you wrote for a prospect as a DRAFT in Kerflow.
        The user reviews it in the Kerflow web app, copies it, sends it from their own mailbox
        and marks it as sent — you never send emails yourself.
        Before writing, call get_prospect for context, and list_prospect_messages if
        messageCount > 0: a follow-up must build on the actual thread, never repeat an
        earlier email. Write in the prospect's language (French for French businesses).
        Creates a new draft each time; it does not overwrite previous ones.""")
    public MessageResult saveProspectEmail(
        @ToolParam(description = "Prospect id (UUID)") String prospectId,
        @ToolParam(description = "Email subject line") String subject,
        @ToolParam(description = "Full email body, plain text, ready to copy-paste into a mail client. No markdown, no unfilled placeholders.") String body,
        ToolContext toolContext) {

        User user = mcpUserContext.requireActiveAccess(toolContext);

        ProspectMessage message = prospectMessageService.createOutboundDraft(
            user, UUID.fromString(prospectId), subject, body, GENERATED_BY);
        return MessageResult.from(message);
    }

    @Tool(name = "record_prospect_reply", description = """
        Record an email reply the prospect sent to the user. Use when the user pastes or
        forwards a prospect's response in the conversation. Store the reply verbatim —
        do not summarize, rephrase or translate it. After recording, you can analyze it
        and propose a follow-up with save_prospect_email. Exchanges on other channels
        (a call, a LinkedIn message) are logged by the user in the web app, not here.""")
    public MessageResult recordProspectReply(
        @ToolParam(description = "Prospect id (UUID)") String prospectId,
        @ToolParam(description = "The prospect's reply, verbatim, including greeting and signature if present") String body,
        @ToolParam(description = "Subject line of the reply if known", required = false) String subject,
        ToolContext toolContext) {

        User user = mcpUserContext.requireActiveAccess(toolContext);

        ProspectMessage message = prospectMessageService.recordInbound(
            user, UUID.fromString(prospectId), subject, body, GENERATED_BY);
        return MessageResult.from(message);
    }

    @Tool(name = "list_prospect_messages", description = """
        Read the full outreach thread of a prospect in chronological order: outbound messages
        (DRAFT = written but not sent yet, SENT = actually sent by the user) and inbound ones
        from the prospect. The thread is not email-only — channel says which support each
        exchange used (EMAIL, PHONE, SMS, LINKEDIN, MEETING, OTHER), and the user logs calls
        and meetings there too, so it is the real state of the relationship. Call this before
        writing any follow-up or analyzing where the conversation stands. get_prospect's
        messageCount tells you whether a thread exists.""")
    public List<MessageResult> listProspectMessages(
        @ToolParam(description = "Prospect id (UUID)") String prospectId,
        ToolContext toolContext) {

        User user = mcpUserContext.currentUser(toolContext);

        return prospectMessageService.listMessages(user, UUID.fromString(prospectId)).stream()
            .map(MessageResult::from)
            .toList();
    }

    @Tool(name = "save_prospect_sheet", description = """
        Save or replace the prospect's profile sheet ("fiche profil"): a markdown briefing
        the user reads before a call and you re-read in future conversations. Overwrites any
        previous sheet, so include everything worth keeping. Structure it with markdown
        headings, e.g.: who they are, what they do, detected needs, decision maker, angle of
        attack, thread status, next step. Build on get_prospect's businessProfile and
        sizeEstimate ground truth and on the message thread. Write it in French.""")
    public ProspectDetail saveProspectSheet(
        @ToolParam(description = "Prospect id (UUID)") String prospectId,
        @ToolParam(description = "Full profile sheet content in markdown") String content,
        ToolContext toolContext) {

        User user = mcpUserContext.requireActiveAccess(toolContext);

        ProfileSheet sheet = new ProfileSheet(content, GENERATED_BY, Instant.now());
        Prospect prospect = prospectService.saveProfileSheet(user, UUID.fromString(prospectId), sheet);
        return ProspectDetail.from(
            prospect,
            prospectScoringService.computeScore(prospect.getSignals()),
            (int) prospectMessageService.countMessages(prospect.getId()));
    }
}
