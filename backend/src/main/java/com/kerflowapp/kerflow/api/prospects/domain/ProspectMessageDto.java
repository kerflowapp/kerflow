package com.kerflowapp.kerflow.api.prospects.domain;

import com.kerflowapp.kerflow.domain.enums.MessageChannel;
import com.kerflowapp.kerflow.domain.enums.MessageDirection;
import com.kerflowapp.kerflow.domain.enums.MessageStatus;
import lombok.Builder;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record ProspectMessageDto(
    UUID id,
    MessageDirection direction,
    MessageChannel channel,
    MessageStatus status,
    String subject,
    String body,
    String generatedBy,
    Instant sentAt,
    Instant receivedAt,
    LocalDateTime creationDate
) {
}
