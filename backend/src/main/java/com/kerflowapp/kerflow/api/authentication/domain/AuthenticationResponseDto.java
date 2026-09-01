package com.kerflowapp.kerflow.api.authentication.domain;

import lombok.Builder;

@Builder
public record AuthenticationResponseDto(
    String token,
    String refreshToken,
    String impersonatedUserEmail
) {
}
