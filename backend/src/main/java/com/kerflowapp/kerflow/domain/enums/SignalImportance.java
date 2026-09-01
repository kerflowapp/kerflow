package com.kerflowapp.kerflow.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Importance of a detected signal. The weight is the raw input of the
 * explainable score (roadmap step 1.1).
 */
@Getter
@RequiredArgsConstructor
public enum SignalImportance {
    HIGH(3),
    MEDIUM(2),
    LOW(1);

    private final int weight;
}
