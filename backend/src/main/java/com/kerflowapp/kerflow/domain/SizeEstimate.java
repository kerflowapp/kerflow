package com.kerflowapp.kerflow.domain;

import com.kerflowapp.kerflow.domain.enums.SizeBucket;
import com.kerflowapp.kerflow.domain.enums.SizeConfidence;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Explainable company size estimate (persisted as jsonb). Like signals, only facts
 * and keys are stored: the frontend translates each reason key (size.reasons.KEY)
 * with its params via i18n.
 */
public record SizeEstimate(
    SizeBucket bucket,
    SizeConfidence confidence,
    List<Reason> reasons,
    Instant estimatedAt
) {

    public record Reason(String key, Map<String, Object> params) {

        public static Reason of(String key, Map<String, Object> params) {
            return new Reason(key, params);
        }
    }

    public static SizeEstimate of(SizeBucket bucket, SizeConfidence confidence, List<Reason> reasons) {
        return new SizeEstimate(bucket, confidence, reasons, Instant.now());
    }

}
