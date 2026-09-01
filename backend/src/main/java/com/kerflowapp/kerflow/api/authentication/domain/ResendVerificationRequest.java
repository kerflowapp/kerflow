package com.kerflowapp.kerflow.api.authentication.domain;

import jakarta.validation.constraints.NotEmpty;

public record ResendVerificationRequest(
    @NotEmpty String email
) {
}
