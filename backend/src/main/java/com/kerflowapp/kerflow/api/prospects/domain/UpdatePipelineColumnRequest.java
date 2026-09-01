package com.kerflowapp.kerflow.api.prospects.domain;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotEmpty;

public record UpdatePipelineColumnRequest(
    @NotEmpty
    String name,
    @Nullable
    String color,
    @Nullable
    String icon
) {
}
