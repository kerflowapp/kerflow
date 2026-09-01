package com.kerflowapp.kerflow.services.scoring;

import java.util.List;

/**
 * Explainable qualification score: never a bare number, always the reasons.
 * score is 0-100, stars is the 1-5 display rating derived from it.
 */
public record ProspectScore(
    int score,
    int stars,
    List<ScoreReason> reasons
) {
}
