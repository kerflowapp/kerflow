package com.kerflowapp.kerflow.mappers;

import com.kerflowapp.kerflow.api.prospects.domain.*;
import com.kerflowapp.kerflow.domain.BusinessProfile;
import com.kerflowapp.kerflow.domain.Prospect;
import com.kerflowapp.kerflow.domain.ProspectSignal;
import com.kerflowapp.kerflow.domain.SizeEstimate;
import com.kerflowapp.kerflow.services.scoring.ProspectScore;
import com.kerflowapp.kerflow.services.scoring.ProspectScoringService;
import com.kerflowapp.kerflow.services.scoring.ScoreReason;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(componentModel = "spring")
public abstract class ProspectMapper {

    @Autowired
    protected ProspectScoringService prospectScoringService;

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "position", ignore = true)
    @Mapping(target = "analysis", ignore = true)
    @Mapping(target = "profileSheet", ignore = true)
    @Mapping(target = "siren", ignore = true)
    @Mapping(target = "sizeEstimate", ignore = true)
    @Mapping(target = "businessProfile", ignore = true)
    public abstract Prospect toEntity(CreateProspectRequest request);

    @Mapping(
        target = "socialLinks",
        expression = "java(new com.kerflowapp.kerflow.api.prospects.domain.SocialLinksDto(prospect.getInstagram(), prospect.getFacebook(), prospect.getLinkedin()))"
    )
    @Mapping(target = "statusKey", expression = "java(prospect.getStatusKey() != null ? prospect.getStatusKey() : (prospect.getStatus() != null ? prospect.getStatus().name() : null))")
    @Mapping(target = "score", expression = "java(toScoreDto(prospectScoringService.computeScore(prospect.getSignals())))")
    public abstract ProspectDto toDto(Prospect prospect);

    public abstract List<ProspectDto> toDtoList(List<Prospect> prospects);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "position", ignore = true)
    @Mapping(target = "source", ignore = true)
    @Mapping(target = "searchQuery", ignore = true)
    @Mapping(target = "signals", ignore = true)
    @Mapping(target = "googlePlaceId", ignore = true)
    @Mapping(target = "googleTypes", ignore = true)
    @Mapping(target = "googleRating", ignore = true)
    @Mapping(target = "googleUserRatingsTotal", ignore = true)
    @Mapping(target = "googleEditorialSummary", ignore = true)
    @Mapping(target = "analysis", ignore = true)
    @Mapping(target = "profileSheet", ignore = true)
    @Mapping(target = "siren", ignore = true)
    @Mapping(target = "sizeEstimate", ignore = true)
    @Mapping(target = "businessProfile", ignore = true)
    @Mapping(target = "creationProcessId", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "lastModificationProcessId", ignore = true)
    @Mapping(target = "lastModificationDate", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    public abstract void updateEntity(@MappingTarget Prospect prospect, UpdateProspectRequest request);

    public abstract SizeEstimateDto toSizeEstimateDto(SizeEstimate sizeEstimate);

    public abstract BusinessProfileDto toBusinessProfileDto(BusinessProfile businessProfile);

    public abstract SignalDto toSignalDto(ProspectSignal signal);

    public abstract List<SignalDto> toSignalDtos(List<ProspectSignal> signals);

    public abstract ProspectScoreDto toScoreDto(ProspectScore score);

    public abstract ScoreReasonDto toScoreReasonDto(ScoreReason reason);

}
