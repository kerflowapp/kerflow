package com.kerflowapp.kerflow.api.prospects.domain;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

public record ReorderPipelineColumnsRequest(
    @NotEmpty
    List<UUID> orderedIds
) {
}
