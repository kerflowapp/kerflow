package com.kerflowapp.kerflow.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Business category of a prospect: what the company does, as opposed to how big it is
 * ({@link SizeBucket}) or how interesting it is ({@link SignalType}). Deliberately a
 * small curated list serving the current persona (fleet software), with OTHER as the
 * honest fallback for everything else.
 * <p>
 * This is a matching dimension, never a quality: it stays out of the score on purpose.
 * Roadmap phase 7 will read it to apply per-target-business weights — which is why it
 * is a typed enum and not a sentence.
 * <p>
 * {@code fleetLikely} means the activity itself implies vehicles (an inference), which
 * is unrelated to the FLEET_DETECTED signal (evidence found on the website).
 */
@Getter
@RequiredArgsConstructor
public enum BusinessCategory {
    TAXI_VTC(true),
    AMBULANCE(true),
    ROAD_FREIGHT(true),
    PASSENGER_TRANSPORT(true),
    VEHICLE_RENTAL(true),
    AUTO_SERVICES(false),
    DRIVING_SCHOOL(true),
    CONSTRUCTION(true),
    LANDSCAPING(true),
    CLEANING(true),
    OTHER(false);

    private final boolean fleetLikely;
}
