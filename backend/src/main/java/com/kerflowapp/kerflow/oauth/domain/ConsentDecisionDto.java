package com.kerflowapp.kerflow.oauth.domain;

import lombok.Builder;

/**
 * Where the SPA must send the browser once the user decided.
 */
@Builder
public record ConsentDecisionDto(
    String redirectUri
) {
}
