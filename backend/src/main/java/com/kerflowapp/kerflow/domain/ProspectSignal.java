package com.kerflowapp.kerflow.domain;

import com.kerflowapp.kerflow.domain.enums.SignalImportance;
import com.kerflowapp.kerflow.domain.enums.SignalType;

import java.io.Serializable;
import java.time.Instant;
import java.util.Map;

/**
 * A neutral fact detected about a prospect (persisted as jsonb).
 * The explanation text is not stored: the frontend translates type + params via i18n.
 */
public record ProspectSignal(
    SignalType type,
    SignalImportance importance,
    String source,
    Map<String, Object> params,
    Instant detectedAt
) implements Serializable {

    public static ProspectSignal of(SignalType type, SignalImportance importance, String source, Map<String, Object> params) {
        return new ProspectSignal(type, importance, source, params, Instant.now());
    }

}
