package com.kerflowapp.kerflow.api.prospects.domain;

import com.kerflowapp.kerflow.domain.enums.MessageChannel;
import com.kerflowapp.kerflow.domain.enums.MessageDirection;
import com.kerflowapp.kerflow.domain.enums.MessageStatus;
import jakarta.validation.constraints.NotBlank;

import java.time.Instant;

/**
 * Replaces every editable field of a message, with the defaults of {@link CreateProspectMessageRequest}:
 * null direction means INBOUND, null channel EMAIL, null status DRAFT (outbound only).
 * A null occurredAt keeps the message's current date.
 */
public record UpdateProspectMessageRequest(
    MessageDirection direction,
    MessageChannel channel,
    MessageStatus status,
    String subject,
    @NotBlank String body,
    Instant occurredAt
) {
}
