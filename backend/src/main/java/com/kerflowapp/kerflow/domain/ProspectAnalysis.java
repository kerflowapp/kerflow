package com.kerflowapp.kerflow.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;
import java.util.List;

/**
 * Agent-written qualification analysis of a prospect (persisted as jsonb).
 * Produced by an MCP client (Claude, ChatGPT...) reasoning over the prospect's signals,
 * size estimate and business profile. It adds the interpretation those cannot compute
 * (specialisation, positioning, buying context) — it never restates them.
 * <p>
 * ignoreUnknown: analyses persisted before the typed SizeEstimate landed carry an
 * "estimatedSize" free-text field that no longer exists here.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ProspectAnalysis(
    String summary,
    List<String> detectedNeeds,
    String suggestedApproach,
    String generatedBy,
    Instant analyzedAt
) {
}
