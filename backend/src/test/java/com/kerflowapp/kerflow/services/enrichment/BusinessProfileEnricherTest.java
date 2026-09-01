package com.kerflowapp.kerflow.services.enrichment;

import com.kerflowapp.kerflow.domain.BusinessProfile;
import com.kerflowapp.kerflow.domain.enums.BusinessCategory;
import com.kerflowapp.kerflow.domain.enums.BusinessCategorySource;
import com.kerflowapp.kerflow.services.business.BusinessCategoryResolver;
import com.kerflowapp.kerflow.services.sirene.CompanyIdentity;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BusinessProfileEnricherTest {

    private final BusinessProfileEnricher enricher = new BusinessProfileEnricher(new BusinessCategoryResolver());

    private List<String> factKeys(EnrichmentContext context) {
        return context.getBusinessProfile().facts().stream().map(BusinessProfile.Fact::key).toList();
    }

    @Test
    void neverRunsDuringSearch() {
        assertThat(enricher.supports(EnrichmentContext.builder().mode(EnrichmentMode.SEARCH).build())).isFalse();
        assertThat(enricher.supports(EnrichmentContext.builder().mode(EnrichmentMode.CREATION).build())).isTrue();
    }

    @Test
    void roadmapCase() {
        // Real company: ASSISTANCE AMBULANCE, NAF 86.90A, created in 1995.
        EnrichmentContext context = EnrichmentContext.builder()
            .companyIdentity(CompanyIdentity.builder()
                .siren("402828909")
                .nomComplet("ASSISTANCE AMBULANCE")
                .activitePrincipale("86.90A")
                .sectionActivitePrincipale("Q")
                .dateCreation(LocalDate.of(1995, 10, 17))
                .build())
            .build();

        enricher.enrich(context);

        BusinessProfile profile = context.getBusinessProfile();
        assertThat(profile.category()).isEqualTo(BusinessCategory.AMBULANCE);
        assertThat(profile.categorySource()).isEqualTo(BusinessCategorySource.NAF);
        assertThat(profile.nafCode()).isEqualTo("86.90A");
        assertThat(profile.nafSection()).isEqualTo("Q");
        assertThat(factKeys(context)).containsExactly("ACTIVITY_NAF", "COMPANY_AGE", "LIKELY_FLEET");
    }

    @Test
    void nafAlwaysWinsOverContradictingGoogleType() {
        EnrichmentContext context = EnrichmentContext.builder()
            .companyIdentity(CompanyIdentity.builder().activitePrincipale("86.90A").sectionActivitePrincipale("Q").build())
            .googleTypes(List.of("car_repair"))
            .build();

        enricher.enrich(context);

        assertThat(context.getBusinessProfile().category()).isEqualTo(BusinessCategory.AMBULANCE);
        assertThat(context.getBusinessProfile().categorySource()).isEqualTo(BusinessCategorySource.NAF);
    }

    @Test
    void googleTypesResolveTheActivityWhenTheRegistrySaidNothing() {
        EnrichmentContext context = EnrichmentContext.builder()
            .googleTypes(List.of("point_of_interest", "taxi_stand"))
            .build();

        enricher.enrich(context);

        BusinessProfile profile = context.getBusinessProfile();
        assertThat(profile.category()).isEqualTo(BusinessCategory.TAXI_VTC);
        assertThat(profile.categorySource()).isEqualTo(BusinessCategorySource.GOOGLE_TYPES);
        assertThat(factKeys(context)).containsExactly("ACTIVITY_GOOGLE", "LIKELY_FLEET");
        assertThat(profile.nafCode()).isNull();
    }

    @Test
    void legacyNafFallsBackToGoogleAndDropsTheWrongSection() {
        // Real case: 85.1J is an ambulance company, but its declared section is P (Education).
        EnrichmentContext context = EnrichmentContext.builder()
            .companyIdentity(CompanyIdentity.builder()
                .activitePrincipale("85.1J")
                .sectionActivitePrincipale("P")
                .build())
            .googleTypes(List.of("car_repair"))
            .build();

        enricher.enrich(context);

        BusinessProfile profile = context.getBusinessProfile();
        assertThat(profile.categorySource()).isEqualTo(BusinessCategorySource.GOOGLE_TYPES);
        assertThat(profile.nafCode()).isEqualTo("85.1J");
        assertThat(profile.nafSection()).isNull();
        assertThat(factKeys(context)).doesNotContain("ACTIVITY_NAF");
    }

    @Test
    void uncuratedNafIsReportedNotGuessed() {
        // Real case: a company named "SARL TAXI" filed under 56.30Z (drinking establishment).
        EnrichmentContext context = EnrichmentContext.builder()
            .companyIdentity(CompanyIdentity.builder()
                .activitePrincipale("56.30Z")
                .sectionActivitePrincipale("I")
                .dateCreation(LocalDate.of(2000, 1, 1))
                .build())
            .build();

        enricher.enrich(context);

        assertThat(context.getBusinessProfile().category()).isEqualTo(BusinessCategory.OTHER);
        assertThat(factKeys(context)).containsExactly("ACTIVITY_NAF_UNMAPPED", "COMPANY_AGE");
        assertThat(factKeys(context)).doesNotContain("LIKELY_FLEET");
    }

    @Test
    void recentCompanyGetsItsOwnFact() {
        EnrichmentContext context = EnrichmentContext.builder()
            .companyIdentity(CompanyIdentity.builder()
                .activitePrincipale("49.32Z")
                .dateCreation(LocalDate.now().minusYears(1))
                .build())
            .build();

        enricher.enrich(context);

        assertThat(factKeys(context)).contains("COMPANY_RECENT").doesNotContain("COMPANY_AGE");
    }

    @Test
    void legalFormFacts() {
        EnrichmentContext context = EnrichmentContext.builder()
            .companyIdentity(CompanyIdentity.builder()
                .activitePrincipale("49.32Z")
                .estEntrepreneurIndividuel(true)
                .build())
            .build();

        enricher.enrich(context);

        assertThat(factKeys(context)).contains("LEGAL_FORM_INDIVIDUAL");
    }

    @Test
    void nothingKnownYieldsNoProfileRatherThanAnEmptyShell() {
        EnrichmentContext context = EnrichmentContext.builder()
            .googleTypes(List.of("point_of_interest", "establishment"))
            .build();

        enricher.enrich(context);

        assertThat(context.getBusinessProfile()).isNull();
    }

    @Test
    void staysScoreNeutral() {
        // A business category is a matching dimension, not a quality: weighting it per target
        // business is roadmap phase 7. Emitting a signal here would bake our own persona into
        // every user's score.
        EnrichmentContext context = EnrichmentContext.builder()
            .companyIdentity(CompanyIdentity.builder()
                .activitePrincipale("86.90A")
                .dateCreation(LocalDate.of(1995, 10, 17))
                .build())
            .build();

        enricher.enrich(context);

        assertThat(context.getSignals()).isEmpty();
    }
}
