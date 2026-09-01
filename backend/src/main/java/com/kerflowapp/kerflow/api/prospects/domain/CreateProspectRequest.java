package com.kerflowapp.kerflow.api.prospects.domain;

import com.kerflowapp.kerflow.domain.enums.ProspectSource;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CreateProspectRequest(
    @NotEmpty
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
    ProspectSource source,
    @Nullable
    String notes,
    @Nullable
    List<String> tags,
    @Nullable
    List<SignalDto> signals,
    @Nullable
    String googlePlaceId,
    @Nullable
    String searchQuery,
    @Nullable
    List<String> googleTypes,
    @Nullable
    Double googleRating,
    @Nullable
    Integer googleUserRatingsTotal,
    @Nullable
    String googleEditorialSummary
) {
}
