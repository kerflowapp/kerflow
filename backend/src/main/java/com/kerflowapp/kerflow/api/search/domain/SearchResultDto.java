package com.kerflowapp.kerflow.api.search.domain;

import com.kerflowapp.kerflow.api.prospects.domain.SignalDto;
import com.kerflowapp.kerflow.services.scoring.ProspectScore;
import lombok.Builder;

import java.util.List;

@Builder(toBuilder = true)
public record SearchResultDto(
    String placeId,
    String name,
    String address,
    String phone,
    String website,
    String googleMapsUrl,
    String instagram,
    String facebook,
    String linkedin,
    String twitter,
    String tiktok,
    String youtube,
    Double rating,
    Integer userRatingsTotal,
    Double lat,
    Double lng,
    List<String> types,
    String businessStatus,
    OpeningHours openingHours,
    List<Review> reviews,
    String editorialSummary,
    Integer score,
    ProspectScore scoreDetails,
    List<SignalDto> signals
) {

    @Builder
    public record OpeningHours(
        boolean openNow,
        List<String> weekdayText
    ) {
    }

    @Builder
    public record Review(
        String authorName,
        String profilePhotoUrl,
        int rating,
        String text,
        String relativeTimeDescription
    ) {
    }
}
