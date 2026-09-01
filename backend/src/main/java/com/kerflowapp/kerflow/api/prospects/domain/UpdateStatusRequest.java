package com.kerflowapp.kerflow.api.prospects.domain;

import jakarta.validation.constraints.NotNull;

public record UpdateStatusRequest(
    @NotNull String statusKey
) {
}
