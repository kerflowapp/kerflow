package com.kerflowapp.kerflow.services.scoring;

import com.kerflowapp.kerflow.domain.enums.SignalImportance;
import com.kerflowapp.kerflow.domain.enums.SignalType;

import java.util.Map;

/**
 * One signal's contribution to the explainable score.
 * No explanation text: the frontend translates type + params via i18n.
 */
public record ScoreReason(
    SignalType type,
    SignalImportance importance,
    int points,
    Map<String, Object> params
) {
}
