package com.kerflowapp.kerflow.api.prospects.domain;

import lombok.Builder;

import java.time.Instant;

@Builder
public record ProfileSheetDto(
    String content,
    String generatedBy,
    Instant generatedAt
) {
}
