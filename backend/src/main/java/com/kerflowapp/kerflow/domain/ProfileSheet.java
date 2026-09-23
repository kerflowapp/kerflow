package com.kerflowapp.kerflow.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.time.Instant;

/**
 * Agent-written prospect profile sheet ("fiche profil"), persisted as jsonb.
 * A markdown briefing the user reads before a call and the agent re-reads in
 * future conversations. Overwritten on every save, like {@link ProspectAnalysis}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ProfileSheet(
    String content,
    String generatedBy,
    Instant generatedAt
) implements Serializable {
}
