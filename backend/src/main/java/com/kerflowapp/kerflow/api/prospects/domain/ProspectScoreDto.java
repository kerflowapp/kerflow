package com.kerflowapp.kerflow.api.prospects.domain;

import lombok.Builder;

import java.util.List;

/**
 * Explainable qualification score: score is 0-100, stars is the 1-5 display
 * rating, reasons list the contributing signals.
 */
@Builder
public record ProspectScoreDto(
    int score,
    int stars,
    List<ScoreReasonDto> reasons
) {
}
