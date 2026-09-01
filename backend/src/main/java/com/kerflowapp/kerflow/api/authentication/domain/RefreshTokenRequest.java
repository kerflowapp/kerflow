package com.kerflowapp.kerflow.api.authentication.domain;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(
    @NotBlank String username,
    @NotBlank String refreshToken
) {
}
