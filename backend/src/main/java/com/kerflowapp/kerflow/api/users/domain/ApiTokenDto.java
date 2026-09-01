package com.kerflowapp.kerflow.api.users.domain;

import lombok.Builder;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record ApiTokenDto(
    UUID id,
    String name,
    String tokenPrefix,
    LocalDateTime creationDate,
    Instant lastUsedAt,
    Instant revokedAt
) {
}
