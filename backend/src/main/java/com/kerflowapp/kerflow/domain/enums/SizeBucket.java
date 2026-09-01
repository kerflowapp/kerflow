package com.kerflowapp.kerflow.domain.enums;

/**
 * Estimated company size bucket. Buckets are people in the company, while the INSEE
 * workforce bracket counts employees (excluding the owner) — hence bracket 01 (1-2
 * employees) maps to FROM_2_TO_5, not SOLO.
 */
public enum SizeBucket {
    SOLO,
    FROM_2_TO_5,
    FROM_6_TO_9,
    FROM_10_TO_19,
    FROM_20_TO_49,
    FIFTY_PLUS
}
