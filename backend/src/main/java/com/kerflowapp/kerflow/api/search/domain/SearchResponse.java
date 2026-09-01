package com.kerflowapp.kerflow.api.search.domain;

import lombok.Builder;

import java.util.List;

@Builder
public record SearchResponse(
    List<SearchResultDto> results,
    String nextPageToken
) {
}
