package com.kerflowapp.kerflow.api.prospects.domain;

import com.kerflowapp.kerflow.domain.enums.SignalImportance;
import com.kerflowapp.kerflow.domain.enums.SignalType;
import lombok.Builder;

import java.util.Map;

/**
 * One signal's contribution to the prospect score. No explanation text:
 * the frontend translates type + params via i18n.
 */
@Builder
public record ScoreReasonDto(
    SignalType type,
    SignalImportance importance,
    int points,
    Map<String, Object> params
) {
}
