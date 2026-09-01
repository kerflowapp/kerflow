package com.kerflowapp.kerflow.mcp.tools;

import com.kerflowapp.kerflow.api.prospects.domain.SignalDto;
import com.kerflowapp.kerflow.api.search.domain.SearchResultDto;
import com.kerflowapp.kerflow.domain.*;
import com.kerflowapp.kerflow.domain.enums.MessageChannel;
import com.kerflowapp.kerflow.services.scoring.ProspectScore;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Slim MCP tool result records. Deliberately smaller than the REST DTOs
 * (reviews truncated, opening hours dropped) to keep agent context usage low.
 */
public final class McpResults {

    private static final int MAX_REVIEWS = 2;
    private static final int MAX_REVIEW_LENGTH = 300;

    private McpResults() {
    }

    private static String isoDate(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    /**
     * Signal as exposed to agents: the detection date is dropped, it adds no
     * reasoning value and bloats every response.
     */
    public record Signal(String type, String importance, String source, Map<String, Object> params) {

        public static Signal from(ProspectSignal signal) {
            return new Signal(
                signal.type() == null ? null : signal.type().name(),
                signal.importance() == null ? null : signal.importance().name(),
                signal.source(),
                signal.params());
        }

        public static Signal from(SignalDto dto) {
            return new Signal(
                dto.type() == null ? null : dto.type().name(),
                dto.importance() == null ? null : dto.importance().name(),
                dto.source(),
                dto.params());
        }

        public static List<Signal> fromSignals(List<ProspectSignal> signals) {
            return signals == null ? List.of() : signals.stream().map(Signal::from).toList();
        }

        public static List<Signal> fromDtos(List<SignalDto> dtos) {
            return dtos == null ? List.of() : dtos.stream().map(Signal::from).toList();
        }
    }

    /**
     * Explainable score as exposed to agents, kept slim: each reason is just the
     * signal type and its signed points (importance/params already sit in signals).
     */
    public record Score(int score, int stars, List<Reason> reasons) {

        public record Reason(String type, int points) {
        }

        public static Score from(ProspectScore score) {
            if (score == null) {
                return null;
            }
            List<Reason> reasons = score.reasons().stream()
                .map(r -> new Reason(r.type() == null ? null : r.type().name(), r.points()))
                .toList();
            return new Score(score.score(), score.stars(), reasons);
        }
    }

    public record Analysis(
        String summary,
        List<String> detectedNeeds,
        String suggestedApproach,
        String analyzedAt
    ) {

        public static Analysis from(ProspectAnalysis analysis) {
            if (analysis == null) {
                return null;
            }
            return new Analysis(
                analysis.summary(),
                analysis.detectedNeeds(),
                analysis.suggestedApproach(),
                analysis.analyzedAt() == null ? null : analysis.analyzedAt().toString());
        }
    }

    /**
     * Agent-written profile sheet as exposed to agents: the markdown briefing kept on
     * the prospect, embedded in ProspectDetail so follow-ups always have it as context.
     */
    public record Sheet(String content, String generatedBy, String generatedAt) {

        public static Sheet from(ProfileSheet sheet) {
            if (sheet == null) {
                return null;
            }
            return new Sheet(
                sheet.content(),
                sheet.generatedBy(),
                sheet.generatedAt() == null ? null : sheet.generatedAt().toString());
        }
    }

    /**
     * One message of a prospect's outreach thread, body untruncated: replies must be
     * analyzable verbatim. Messages predating channels report EMAIL: an agent should never
     * have to reason about a null support.
     */
    public record MessageResult(
        UUID id,
        String direction,
        String channel,
        String status,
        String subject,
        String body,
        String generatedBy,
        String sentAt,
        String receivedAt,
        String createdAt
    ) {

        public static MessageResult from(ProspectMessage message) {
            return new MessageResult(
                message.getId(),
                message.getDirection() == null ? null : message.getDirection().name(),
                message.getChannel() == null ? MessageChannel.EMAIL.name() : message.getChannel().name(),
                message.getStatus() == null ? null : message.getStatus().name(),
                message.getSubject(),
                message.getBody(),
                message.getGeneratedBy(),
                message.getSentAt() == null ? null : message.getSentAt().toString(),
                message.getReceivedAt() == null ? null : message.getReceivedAt().toString(),
                isoDate(message.getCreationDate()));
        }
    }

    /**
     * Computed size estimate as exposed to agents. Reason keys are given raw: they are
     * i18n keys, and an agent reads SIRENE_WORKFORCE_BRACKET fine.
     */
    public record Size(String bucket, String confidence, List<String> reasons) {

        public static Size from(SizeEstimate estimate) {
            if (estimate == null) {
                return null;
            }
            List<String> reasons = estimate.reasons() == null ? List.of() : estimate.reasons().stream()
                .map(SizeEstimate.Reason::key)
                .toList();
            return new Size(
                estimate.bucket() == null ? null : estimate.bucket().name(),
                estimate.confidence() == null ? null : estimate.confidence().name(),
                reasons);
        }
    }

    /**
     * Deterministic business profile as exposed to agents: the ground truth they should
     * build on instead of guessing the activity — and never restate.
     */
    public record Business(
        String category,
        String categorySource,
        String nafCode,
        String nafSection,
        List<String> facts
    ) {

        public static Business from(BusinessProfile profile) {
            if (profile == null) {
                return null;
            }
            List<String> facts = profile.facts() == null ? List.of() : profile.facts().stream()
                .map(BusinessProfile.Fact::key)
                .toList();
            return new Business(
                profile.category() == null ? null : profile.category().name(),
                profile.categorySource() == null ? null : profile.categorySource().name(),
                profile.nafCode(),
                profile.nafSection(),
                facts);
        }
    }

    public record BusinessSearchResult(
        List<BusinessResult> results,
        String nextPageToken
    ) {
    }

    public record BusinessResult(
        String placeId,
        String name,
        String address,
        String phone,
        String website,
        Double rating,
        Integer reviewCount,
        String businessStatus,
        List<String> types,
        String editorialSummary,
        Score score,
        String instagram,
        String facebook,
        String linkedin,
        List<Signal> signals,
        List<String> reviewExcerpts
    ) {

        public static BusinessResult from(SearchResultDto dto) {
            List<String> reviewExcerpts = dto.reviews() == null ? List.of() : dto.reviews().stream()
                .limit(MAX_REVIEWS)
                .map(review -> truncate(review.text()))
                .filter(text -> text != null && !text.isBlank())
                .toList();

            return new BusinessResult(
                dto.placeId(),
                dto.name(),
                dto.address(),
                dto.phone(),
                dto.website(),
                dto.rating(),
                dto.userRatingsTotal(),
                dto.businessStatus(),
                dto.types(),
                dto.editorialSummary(),
                Score.from(dto.scoreDetails()),
                dto.instagram(),
                dto.facebook(),
                dto.linkedin(),
                Signal.fromDtos(dto.signals()),
                reviewExcerpts);
        }

        private static String truncate(String text) {
            if (text == null || text.length() <= MAX_REVIEW_LENGTH) {
                return text;
            }
            return text.substring(0, MAX_REVIEW_LENGTH) + "…";
        }
    }

    public record ProspectSummary(
        UUID id,
        String name,
        String statusKey,
        String address,
        String website,
        String phone,
        List<String> tags,
        int signalCount,
        Integer score,
        boolean hasAnalysis,
        String creationDate
    ) {

        public static ProspectSummary from(Prospect prospect, ProspectScore score) {
            return new ProspectSummary(
                prospect.getId(),
                prospect.getName(),
                prospect.getStatusKey(),
                prospect.getAddress(),
                prospect.getWebsite(),
                prospect.getPhone(),
                prospect.getTags(),
                prospect.getSignals() == null ? 0 : prospect.getSignals().size(),
                score == null ? null : score.score(),
                prospect.getAnalysis() != null,
                isoDate(prospect.getCreationDate()));
        }
    }

    public record ProspectDetail(
        UUID id,
        String name,
        String address,
        String phone,
        String email,
        String website,
        String statusKey,
        String source,
        String notes,
        List<String> tags,
        String googlePlaceId,
        String searchQuery,
        String instagram,
        String facebook,
        String linkedin,
        List<Signal> signals,
        Score score,
        Size sizeEstimate,
        Business businessProfile,
        String googleEditorialSummary,
        Analysis analysis,
        Sheet profileSheet,
        int messageCount,
        String creationDate
    ) {

        public static ProspectDetail from(Prospect prospect, ProspectScore score, int messageCount) {
            return new ProspectDetail(
                prospect.getId(),
                prospect.getName(),
                prospect.getAddress(),
                prospect.getPhone(),
                prospect.getEmail(),
                prospect.getWebsite(),
                prospect.getStatusKey(),
                prospect.getSource() == null ? null : prospect.getSource().name(),
                prospect.getNotes(),
                prospect.getTags(),
                prospect.getGooglePlaceId(),
                prospect.getSearchQuery(),
                prospect.getInstagram(),
                prospect.getFacebook(),
                prospect.getLinkedin(),
                Signal.fromSignals(prospect.getSignals()),
                Score.from(score),
                Size.from(prospect.getSizeEstimate()),
                Business.from(prospect.getBusinessProfile()),
                prospect.getGoogleEditorialSummary(),
                Analysis.from(prospect.getAnalysis()),
                Sheet.from(prospect.getProfileSheet()),
                messageCount,
                isoDate(prospect.getCreationDate()));
        }
    }
}
