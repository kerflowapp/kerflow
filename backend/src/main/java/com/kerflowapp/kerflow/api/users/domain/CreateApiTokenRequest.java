package com.kerflowapp.kerflow.api.users.domain;

import jakarta.validation.constraints.NotEmpty;

public record CreateApiTokenRequest(
    @NotEmpty
    String name
) {
}
