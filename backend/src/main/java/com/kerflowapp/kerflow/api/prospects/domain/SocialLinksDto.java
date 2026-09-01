package com.kerflowapp.kerflow.api.prospects.domain;

import jakarta.annotation.Nullable;

public record SocialLinksDto(
    @Nullable String instagram,
    @Nullable String facebook,
    @Nullable String linkedin
) {
}
