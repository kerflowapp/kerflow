package com.kerflowapp.kerflow.api.prospects.domain;

import com.kerflowapp.kerflow.domain.enums.KanbanStatus;
import com.kerflowapp.kerflow.domain.enums.ProspectSource;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Builder
public record ProspectDto(
    UUID id,
    String name,
    String address,
    String phone,
    String email,
    String website,
    Double lat,
    Double lng,
    SocialLinksDto socialLinks,
    String instagram,
    String facebook,
    String linkedin,
    KanbanStatus status,
    String statusKey,
    Integer position,
    ProspectSource source,
    String notes,
    List<String> tags,
    List<SignalDto> signals,
    ProspectScoreDto score,
    String googlePlaceId,
    String searchQuery,
    List<String> googleTypes,
    Double googleRating,
    Integer googleUserRatingsTotal,
    String googleEditorialSummary,
    String siren,
    SizeEstimateDto sizeEstimate,
    BusinessProfileDto businessProfile,
    ProspectAnalysisDto analysis,
    ProfileSheetDto profileSheet,
    LocalDateTime creationDate,
    LocalDateTime lastModificationDate
) {
}
