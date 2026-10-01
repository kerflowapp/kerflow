package com.kerflowapp.kerflow.api.users.domain;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record OAuthConnectionDto(
    UUID clientId,
    String clientName,
    LocalDateTime lastActivity
) {
}
