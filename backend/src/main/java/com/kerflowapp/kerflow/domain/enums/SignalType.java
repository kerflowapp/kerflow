package com.kerflowapp.kerflow.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * A detectable fact about a prospect. The polarity is the static direction of
 * the signal in the explainable score (+1 qualifies, -1 disqualifies, 0 neutral).
 * Per-business configurable weights will come with roadmap phase 7.
 */
@Getter
@RequiredArgsConstructor
public enum SignalType {
    MANY_REVIEWS(1),
    MOBILE_ONLY_PHONE(-1),
    NO_WEBSITE(-1),
    NO_HTTPS(-1),
    WORDPRESS(0),
    PRO_EMAIL(1),
    GENERIC_EMAIL(-1),
    RECRUITING_DETECTED(1),
    FLEET_DETECTED(1),
    PROFESSIONAL_WEBSITE(1),
    ACTIVE_GOOGLE_PRESENCE(1),
    SOCIAL_MEDIA_PRESENCE(1),
    MULTI_ESTABLISHMENT(1),
    COMPANY_MATURE(1);

    private final int polarity;
}
