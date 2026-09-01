package com.kerflowapp.kerflow.api.authentication.domain;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record NotificationDTO(
    UUID id,
    LocalDateTime creationDate,
    String title,
    String content,
    String type,
    String url,
    boolean isRead
) {
}
