package com.kerflowapp.kerflow.domain.enums;

/**
 * Confidence of a size estimate: HIGH when backed by a declared INSEE workforce
 * bracket, MEDIUM/LOW when derived from heuristics or stale data.
 */
public enum SizeConfidence {
    LOW,
    MEDIUM,
    HIGH
}
