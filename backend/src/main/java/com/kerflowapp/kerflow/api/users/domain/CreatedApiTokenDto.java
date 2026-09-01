package com.kerflowapp.kerflow.api.users.domain;

import lombok.Builder;

import java.util.UUID;

/**
 * Returned once at creation: {@code token} is the plaintext, never retrievable again.
 */
@Builder
public record CreatedApiTokenDto(
    UUID id,
    String name,
    String tokenPrefix,
    String token
) {
}
