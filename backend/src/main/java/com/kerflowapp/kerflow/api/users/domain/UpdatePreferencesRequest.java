package com.kerflowapp.kerflow.api.users.domain;

import com.kerflowapp.kerflow.services.prospects.FollowUpService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdatePreferencesRequest(
    @NotNull
    @Min(FollowUpService.MIN_DELAY_DAYS)
    @Max(FollowUpService.MAX_DELAY_DAYS)
    Integer followUpDelayDays
) {
}
