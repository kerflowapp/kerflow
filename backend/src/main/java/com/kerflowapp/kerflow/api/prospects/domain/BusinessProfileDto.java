package com.kerflowapp.kerflow.api.prospects.domain;

import com.kerflowapp.kerflow.domain.enums.BusinessCategory;
import com.kerflowapp.kerflow.domain.enums.BusinessCategorySource;
import lombok.Builder;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Builder
public record BusinessProfileDto(
    BusinessCategory category,
    BusinessCategorySource categorySource,
    String nafCode,
    String nafSection,
    List<String> googleTypes,
    List<FactDto> facts,
    Instant profiledAt
) {

    public record FactDto(
        String key,
        Map<String, Object> params
    ) {
    }

}
