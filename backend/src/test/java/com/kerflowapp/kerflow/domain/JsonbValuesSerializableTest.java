package com.kerflowapp.kerflow.domain;

import com.kerflowapp.kerflow.domain.enums.BusinessCategory;
import com.kerflowapp.kerflow.domain.enums.BusinessCategorySource;
import com.kerflowapp.kerflow.domain.enums.SignalImportance;
import com.kerflowapp.kerflow.domain.enums.SignalType;
import com.kerflowapp.kerflow.domain.enums.SizeBucket;
import com.kerflowapp.kerflow.domain.enums.SizeConfidence;
import org.hibernate.internal.util.SerializationHelper;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Hypersistence's JsonType deep-copies jsonb attributes through Java serialization and fails
 * on anything that is not Serializable, so every value stored in a jsonb column must be.
 */
class JsonbValuesSerializableTest {

    @Test
    void prospect_signal_survives_a_hibernate_deep_copy() {
        assertCloneable(ProspectSignal.of(SignalType.MANY_REVIEWS, SignalImportance.HIGH, "google-places",
            Map.of("count", 1158, "rating", 4.6)));
    }

    @Test
    void business_profile_survives_a_hibernate_deep_copy() {
        assertCloneable(BusinessProfile.of(BusinessCategory.OTHER, BusinessCategorySource.NAF, "56.10A", "I",
            List.of("restaurant"), List.of(BusinessProfile.Fact.of("COMPANY_AGE", Map.of("year", 2005)))));
    }

    @Test
    void size_estimate_survives_a_hibernate_deep_copy() {
        assertCloneable(SizeEstimate.of(SizeBucket.SOLO, SizeConfidence.LOW,
            List.of(SizeEstimate.Reason.of("HEADCOUNT", Map.of("count", 1)))));
    }

    @Test
    void analysis_and_profile_sheet_survive_a_hibernate_deep_copy() {
        assertCloneable(new ProspectAnalysis("summary", List.of("need"), "approach", "agent", Instant.now()));
        assertCloneable(new ProfileSheet("content", "agent", Instant.now()));
    }

    private static void assertCloneable(Serializable value) {
        assertThat(SerializationHelper.clone(value)).isEqualTo(value);
    }
}
