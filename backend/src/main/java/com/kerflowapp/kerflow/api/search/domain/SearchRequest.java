package com.kerflowapp.kerflow.api.search.domain;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotEmpty;

public record SearchRequest(
    @NotEmpty
    String keywords,
    @NotEmpty
    String city,
    @Nullable
    Integer radius,
    @Nullable
    Double lat,
    @Nullable
    Double lng,
    @Nullable
    String pageToken
) {
}
