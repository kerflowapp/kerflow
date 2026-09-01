package com.kerflowapp.kerflow.domain.enums;

/**
 * Where a {@link BusinessCategory} comes from. The source is the confidence: NAF is the
 * declared activity from the official company registry, GOOGLE_TYPES is a guess from the
 * Google listing that nobody confirmed.
 */
public enum BusinessCategorySource {
    NAF,
    GOOGLE_TYPES
}
