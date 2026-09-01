package com.kerflowapp.kerflow.api.prospects.domain;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record ProspectPipelineColumnDto(
    UUID id,
    String key,
    String name,
    Integer sortOrder,
    String color,
    String icon,
    Boolean system,
    LocalDateTime creationDate,
    LocalDateTime lastModificationDate
) {
}
