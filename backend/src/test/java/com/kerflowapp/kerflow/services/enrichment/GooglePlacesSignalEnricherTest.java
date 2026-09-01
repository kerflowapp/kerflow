package com.kerflowapp.kerflow.services.enrichment;

import com.kerflowapp.kerflow.domain.ProspectSignal;
import com.kerflowapp.kerflow.domain.enums.SignalType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GooglePlacesSignalEnricherTest {

    private final GooglePlacesSignalEnricher enricher = new GooglePlacesSignalEnricher();

    private List<SignalType> enrich(EnrichmentContext context) {
        enricher.enrich(context);
        return context.getSignals().stream().map(ProspectSignal::type).toList();
    }

    @Test
    void detectsActiveGooglePresenceWhenRatingAndReviewsAreHighEnough() {
        EnrichmentContext context = EnrichmentContext.builder()
            .rating(4.5)
            .userRatingsTotal(30)
            .build();

        assertThat(enrich(context)).containsExactly(SignalType.ACTIVE_GOOGLE_PRESENCE);
        assertThat(context.getSignals().getFirst().params())
            .containsEntry("rating", 4.5)
            .containsEntry("count", 30);
    }

    @Test
    void noActivePresenceWhenRatingTooLowOrTooFewReviews() {
        EnrichmentContext lowRating = EnrichmentContext.builder()
            .rating(3.9)
            .userRatingsTotal(100)
            .build();
        EnrichmentContext fewReviews = EnrichmentContext.builder()
            .rating(4.8)
            .userRatingsTotal(19)
            .build();
        EnrichmentContext noData = EnrichmentContext.builder().build();

        assertThat(enrich(lowRating)).doesNotContain(SignalType.ACTIVE_GOOGLE_PRESENCE);
        assertThat(enrich(fewReviews)).doesNotContain(SignalType.ACTIVE_GOOGLE_PRESENCE);
        assertThat(enrich(noData)).isEmpty();
    }

    @Test
    void manyReviewsAndActivePresenceCanCoexist() {
        EnrichmentContext context = EnrichmentContext.builder()
            .rating(4.2)
            .userRatingsTotal(250)
            .build();

        assertThat(enrich(context))
            .containsExactlyInAnyOrder(SignalType.MANY_REVIEWS, SignalType.ACTIVE_GOOGLE_PRESENCE);
    }

    @Test
    void detectsMobileOnlyPhone() {
        EnrichmentContext context = EnrichmentContext.builder()
            .phone("06 12 34 56 78")
            .build();

        assertThat(enrich(context)).containsExactly(SignalType.MOBILE_ONLY_PHONE);
    }
}
