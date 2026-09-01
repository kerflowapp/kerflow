package com.kerflowapp.kerflow.api.prospects.domain;

import lombok.Builder;

import java.time.Instant;
import java.util.List;

@Builder
public record ProspectAnalysisDto(
    String summary,
    List<String> detectedNeeds,
    String suggestedApproach,
    String generatedBy,
    Instant analyzedAt
) {
}
