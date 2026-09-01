package com.kerflowapp.kerflow.services.enrichment;

import com.kerflowapp.kerflow.domain.enums.SignalImportance;
import com.kerflowapp.kerflow.domain.enums.SignalType;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Signals computed in memory from the Google Places data already present on the context.
 * <p>
 * TODO RESPONDS_TO_REVIEWS: neither the legacy nor the New Places API returns owner replies
 * in {@code reviews}, and the Business Profile API is restricted to the listing owner.
 * Needs Google Maps scraping or a third-party source (see roadmap "Différé").
 */
@Component
public class GooglePlacesSignalEnricher implements Enricher {

    private static final String ID = "google-places";
    private static final int MANY_REVIEWS_THRESHOLD = 200;
    private static final double ACTIVE_PRESENCE_MIN_RATING = 4.0;
    private static final int ACTIVE_PRESENCE_MIN_REVIEWS = 20;

    @Override
    public String id() {
        return ID;
    }

    @Override
    public int order() {
        return 10;
    }

    /**
     * On creation these signals already came from the search — recomputing them would only
     * duplicate what the frontend just sent.
     */
    @Override
    public boolean supports(EnrichmentContext context) {
        return context.getMode() != EnrichmentMode.CREATION;
    }

    @Override
    public void enrich(EnrichmentContext context) {
        Integer reviewCount = context.getUserRatingsTotal();
        if (reviewCount != null && reviewCount > MANY_REVIEWS_THRESHOLD) {
            context.addSignal(SignalType.MANY_REVIEWS, SignalImportance.HIGH, ID, Map.of("count", reviewCount));
        }

        Double rating = context.getRating();
        if (rating != null && rating >= ACTIVE_PRESENCE_MIN_RATING
            && reviewCount != null && reviewCount >= ACTIVE_PRESENCE_MIN_REVIEWS) {
            context.addSignal(SignalType.ACTIVE_GOOGLE_PRESENCE, SignalImportance.MEDIUM, ID,
                Map.of("rating", rating, "count", reviewCount));
        }

        if (isFrenchMobileNumber(context.getPhone())) {
            context.addSignal(SignalType.MOBILE_ONLY_PHONE, SignalImportance.MEDIUM, ID, null);
        }
    }

    private boolean isFrenchMobileNumber(String phone) {
        if (phone == null || phone.isBlank()) {
            return false;
        }
        String normalized = phone.replaceAll("[\\s.\\-()]", "");
        return normalized.startsWith("06")
            || normalized.startsWith("07")
            || normalized.startsWith("+336")
            || normalized.startsWith("+337")
            || normalized.startsWith("00336")
            || normalized.startsWith("00337");
    }
}
