package com.kerflowapp.kerflow.api.prospects.domain;

import jakarta.annotation.Nullable;

import java.util.List;

public record UpdateProspectRequest(
    @Nullable
    String name,
    @Nullable
    String address,
    @Nullable
    String phone,
    @Nullable
    String email,
    @Nullable
    String website,
    @Nullable
    Double lat,
    @Nullable
    Double lng,
    @Nullable
    SocialLinksDto socialLinks,
    @Nullable
    String instagram,
    @Nullable
    String facebook,
    @Nullable
    String linkedin,
    @Nullable
    String statusKey,
    @Nullable
    String notes,
    @Nullable
    List<String> tags
) {
}
