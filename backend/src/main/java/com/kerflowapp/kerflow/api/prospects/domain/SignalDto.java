package com.kerflowapp.kerflow.api.prospects.domain;

import com.kerflowapp.kerflow.domain.enums.SignalImportance;
import com.kerflowapp.kerflow.domain.enums.SignalType;
import lombok.Builder;

import java.time.Instant;
import java.util.Map;

@Builder
public record SignalDto(
    SignalType type,
    SignalImportance importance,
    String source,
    Map<String, Object> params,
    Instant detectedAt
) {
}
