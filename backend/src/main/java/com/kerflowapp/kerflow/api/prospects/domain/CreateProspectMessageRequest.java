package com.kerflowapp.kerflow.api.prospects.domain;

import com.kerflowapp.kerflow.domain.enums.MessageChannel;
import com.kerflowapp.kerflow.domain.enums.MessageDirection;
import com.kerflowapp.kerflow.domain.enums.MessageStatus;
import jakarta.validation.constraints.NotBlank;

import java.time.Instant;

/**
 * Everything but the body is optional, so the "paste a reply" case stays a two-field form:
 * null direction defaults to INBOUND, null channel to EMAIL, null occurredAt to now.
 * status is outbound-only and defaults to DRAFT; it is ignored on inbound messages.
 */
public record CreateProspectMessageRequest(
    MessageDirection direction,
    MessageChannel channel,
    MessageStatus status,
    String subject,
    @NotBlank String body,
    Instant occurredAt
) {
}
