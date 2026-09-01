package com.kerflowapp.kerflow.api.prospects.domain;

import com.kerflowapp.kerflow.domain.enums.SizeBucket;
import com.kerflowapp.kerflow.domain.enums.SizeConfidence;
import lombok.Builder;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Builder
public record SizeEstimateDto(
    SizeBucket bucket,
    SizeConfidence confidence,
    List<ReasonDto> reasons,
    Instant estimatedAt
) {

    public record ReasonDto(
        String key,
        Map<String, Object> params
    ) {
    }
}
