package com.kerflowapp.kerflow.api.authentication.domain;

import com.kerflowapp.kerflow.domain.enums.BillingInterval;
import com.kerflowapp.kerflow.domain.enums.SubscriptionStatus;
import lombok.Builder;

import java.time.Instant;

@Builder
public record SubscriptionDto(
    SubscriptionStatus status,
    BillingInterval interval,
    Instant trialEndsAt,
    Instant currentPeriodEnd,
    Instant cancelledAt,
    boolean hasAccess
) {
}
