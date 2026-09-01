package com.kerflowapp.kerflow.oauth.domain;

import lombok.Builder;

import java.time.Instant;
import java.util.List;

/**
 * What the consent screen needs to tell the user who is asking for what.
 */
@Builder
public record AuthorizationRequestDto(
    String clientName,
    List<String> scopes,
    Instant expiresAt
) {
}
