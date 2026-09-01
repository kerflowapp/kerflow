package com.kerflowapp.kerflow.services.business;

import com.kerflowapp.kerflow.domain.enums.BusinessCategory;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BusinessCategoryResolverTest {

    private final BusinessCategoryResolver resolver = new BusinessCategoryResolver();

    @Test
    void resolvesCuratedNafCodes() {
        assertThat(resolver.resolveFromNaf("49.32Z")).contains(BusinessCategory.TAXI_VTC);
        assertThat(resolver.resolveFromNaf("86.90A")).contains(BusinessCategory.AMBULANCE);
        assertThat(resolver.resolveFromNaf("49.41A")).contains(BusinessCategory.ROAD_FREIGHT);
        assertThat(resolver.resolveFromNaf("77.11A")).contains(BusinessCategory.VEHICLE_RENTAL);
        assertThat(resolver.resolveFromNaf("85.53Z")).contains(BusinessCategory.DRIVING_SCHOOL);
        assertThat(resolver.resolveFromNaf("81.30Z")).contains(BusinessCategory.LANDSCAPING);
    }

    @Test
    void shortPrefixesCoverWholeSections() {
        // The whole construction section, so every craftsman resolves without a 732-entry table.
        assertThat(resolver.resolveFromNaf("43.22A")).contains(BusinessCategory.CONSTRUCTION);
        assertThat(resolver.resolveFromNaf("43.91B")).contains(BusinessCategory.CONSTRUCTION);
        assertThat(resolver.resolveFromNaf("41.20A")).contains(BusinessCategory.CONSTRUCTION);
        assertThat(resolver.resolveFromNaf("42.11Z")).contains(BusinessCategory.CONSTRUCTION);

        assertThat(resolver.resolveFromNaf("81.21Z")).contains(BusinessCategory.CLEANING);
        assertThat(resolver.resolveFromNaf("81.22Z")).contains(BusinessCategory.CLEANING);
    }

    @Test
    void validButUncuratedCodeIsOther() {
        assertThat(resolver.resolveFromNaf("62.01Z")).contains(BusinessCategory.OTHER);
        assertThat(resolver.resolveFromNaf("00.00Z")).contains(BusinessCategory.OTHER);
        assertThat(resolver.resolveFromNaf("56.30Z")).contains(BusinessCategory.OTHER);
    }

    @Test
    void legacyCodeIsNotInterpretedAtAll() {
        // Real case: "AMBULANCE AGREE AMBULANCE NANTAISE" is filed under the legacy rev.1 code
        // 85.1J and declares section P (Education). Interpreting it would show "Education" on
        // an ambulance company, so a non-rev.2 shape yields nothing at all.
        assertThat(resolver.isInterpretableNaf("85.1J")).isFalse();
        assertThat(resolver.resolveFromNaf("85.1J")).isEmpty();

        assertThat(resolver.isInterpretableNaf("84.13")).isFalse();
        assertThat(resolver.resolveFromNaf(null)).isEmpty();
        assertThat(resolver.resolveFromNaf("")).isEmpty();

        assertThat(resolver.isInterpretableNaf("86.90A")).isTrue();
    }

    @Test
    void resolvesMappedGoogleTypes() {
        assertThat(resolver.resolveFromGoogleTypes(List.of("taxi_stand")))
            .get()
            .extracting(BusinessCategoryResolver.GoogleTypeMatch::category, BusinessCategoryResolver.GoogleTypeMatch::googleType)
            .containsExactly(BusinessCategory.TAXI_VTC, "taxi_stand");

        assertThat(resolver.resolveFromGoogleTypes(List.of("plumber")))
            .get()
            .extracting(BusinessCategoryResolver.GoogleTypeMatch::category)
            .isEqualTo(BusinessCategory.CONSTRUCTION);
    }

    @Test
    void genericGoogleTypesResolveToNothing() {
        // Every listing carries these: mapping them would categorise the whole world.
        assertThat(resolver.resolveFromGoogleTypes(List.of("point_of_interest", "establishment"))).isEmpty();
        // A clinic is not an ambulance company: deliberately unmapped rather than wrong.
        assertThat(resolver.resolveFromGoogleTypes(List.of("hospital", "doctor"))).isEmpty();
        assertThat(resolver.resolveFromGoogleTypes(List.of())).isEmpty();
        assertThat(resolver.resolveFromGoogleTypes(null)).isEmpty();
    }

    @Test
    void firstMappedTypeWinsOverGenericOnes() {
        assertThat(resolver.resolveFromGoogleTypes(List.of("point_of_interest", "car_repair", "establishment")))
            .get()
            .extracting(BusinessCategoryResolver.GoogleTypeMatch::category)
            .isEqualTo(BusinessCategory.AUTO_SERVICES);
    }
}
