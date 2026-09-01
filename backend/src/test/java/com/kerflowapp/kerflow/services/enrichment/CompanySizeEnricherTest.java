package com.kerflowapp.kerflow.services.enrichment;

import com.kerflowapp.kerflow.domain.SizeEstimate;
import com.kerflowapp.kerflow.domain.enums.SignalImportance;
import com.kerflowapp.kerflow.domain.enums.SignalType;
import com.kerflowapp.kerflow.domain.enums.SizeBucket;
import com.kerflowapp.kerflow.domain.enums.SizeConfidence;
import com.kerflowapp.kerflow.services.sirene.CompanyIdentity;
import com.kerflowapp.kerflow.services.sirene.CompanyNameMatcher;
import com.kerflowapp.kerflow.services.sirene.RechercheEntreprisesClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.Year;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class CompanySizeEnricherTest {

    private RechercheEntreprisesClient client;
    private CompanySizeEnricher enricher;

    @BeforeEach
    void setUp() {
        client = mock(RechercheEntreprisesClient.class);
        enricher = new CompanySizeEnricher(client, new CompanyNameMatcher());
    }

    private EnrichmentContext contextFor(String name) {
        return EnrichmentContext.builder()
            .mode(EnrichmentMode.FULL)
            .name(name)
            .address("12 rue des Lilas, 44000 Nantes")
            .build();
    }

    private CompanyIdentity.CompanyIdentityBuilder assistanceAmbulance() {
        return CompanyIdentity.builder()
            .siren("402828909")
            .nomComplet("ASSISTANCE AMBULANCE")
            .activitePrincipale("86.90A")
            .dateCreation(LocalDate.of(1995, 10, 17))
            .nombreEtablissementsOuverts(2)
            .etatAdministratif("A");
    }

    @Test
    void neverRunsDuringSearch() {
        EnrichmentContext context = EnrichmentContext.builder().mode(EnrichmentMode.SEARCH).build();

        assertThat(enricher.supports(context)).isFalse();
        assertThat(enricher.supports(EnrichmentContext.builder().mode(EnrichmentMode.CREATION).build())).isTrue();
        assertThat(enricher.supports(EnrichmentContext.builder().mode(EnrichmentMode.FULL).build())).isTrue();
    }

    @Test
    void declaredWorkforceBracketGivesHighConfidence() {
        when(client.search(anyString(), anyString())).thenReturn(List.of(assistanceAmbulance()
            .trancheEffectifSalarie("12")
            .anneeTrancheEffectifSalarie(Year.now().getValue() - 1)
            .build()));

        EnrichmentContext context = contextFor("Assistance Ambulance");
        enricher.enrich(context);

        SizeEstimate estimate = context.getSizeEstimate();
        assertThat(estimate.bucket()).isEqualTo(SizeBucket.FROM_20_TO_49);
        assertThat(estimate.confidence()).isEqualTo(SizeConfidence.HIGH);
        assertThat(estimate.reasons()).extracting(SizeEstimate.Reason::key)
            .containsExactly("SIRENE_WORKFORCE_BRACKET");
        assertThat(context.getSiren()).isEqualTo("402828909");
    }

    @Test
    void staleBracketDegradesToMedium() {
        when(client.search(anyString(), anyString())).thenReturn(List.of(assistanceAmbulance()
            .trancheEffectifSalarie("03")
            .anneeTrancheEffectifSalarie(Year.now().getValue() - 8)
            .build()));

        EnrichmentContext context = contextFor("Assistance Ambulance");
        enricher.enrich(context);

        assertThat(context.getSizeEstimate().bucket()).isEqualTo(SizeBucket.FROM_6_TO_9);
        assertThat(context.getSizeEstimate().confidence()).isEqualTo(SizeConfidence.MEDIUM);
    }

    @Test
    void bracketsMapEmployeesToPeopleInTheCompany() {
        // INSEE brackets count employees excluding the owner: bracket 01 is 1-2 employees,
        // so 2-3 people in the company.
        assertThat(bucketFor("00")).isEqualTo(SizeBucket.SOLO);
        assertThat(bucketFor("01")).isEqualTo(SizeBucket.FROM_2_TO_5);
        assertThat(bucketFor("02")).isEqualTo(SizeBucket.FROM_2_TO_5);
        assertThat(bucketFor("03")).isEqualTo(SizeBucket.FROM_6_TO_9);
        assertThat(bucketFor("11")).isEqualTo(SizeBucket.FROM_10_TO_19);
        assertThat(bucketFor("12")).isEqualTo(SizeBucket.FROM_20_TO_49);
        assertThat(bucketFor("21")).isEqualTo(SizeBucket.FIFTY_PLUS);
        assertThat(bucketFor("53")).isEqualTo(SizeBucket.FIFTY_PLUS);
    }

    private SizeBucket bucketFor(String bracket) {
        when(client.search(anyString(), anyString())).thenReturn(List.of(assistanceAmbulance()
            .trancheEffectifSalarie(bracket)
            .anneeTrancheEffectifSalarie(Year.now().getValue())
            .build()));

        EnrichmentContext context = contextFor("Assistance Ambulance");
        enricher.enrich(context);
        return context.getSizeEstimate().bucket();
    }

    @Test
    void addsRegistrySignals() {
        when(client.search(anyString(), anyString())).thenReturn(List.of(assistanceAmbulance()
            .trancheEffectifSalarie("12")
            .anneeTrancheEffectifSalarie(Year.now().getValue())
            .build()));

        EnrichmentContext context = contextFor("Assistance Ambulance");
        enricher.enrich(context);

        assertThat(context.getSignals())
            .extracting(signal -> signal.type())
            .containsExactlyInAnyOrder(SignalType.MULTI_ESTABLISHMENT, SignalType.COMPANY_MATURE);
        assertThat(context.getSignals())
            .filteredOn(signal -> signal.type() == SignalType.MULTI_ESTABLISHMENT)
            .allSatisfy(signal -> {
                assertThat(signal.importance()).isEqualTo(SignalImportance.MEDIUM);
                assertThat(signal.params()).containsEntry("count", 2);
            });
    }

    @Test
    void putsTheMatchedCompanyOnTheContextForLaterEnrichers() {
        // BusinessProfileEnricher depends on this: it reads the identity instead of calling
        // the API a second time.
        when(client.search(anyString(), anyString())).thenReturn(List.of(assistanceAmbulance()
            .trancheEffectifSalarie("12")
            .build()));

        EnrichmentContext context = contextFor("Assistance Ambulance");
        enricher.enrich(context);

        assertThat(context.getCompanyIdentity()).isNotNull();
        assertThat(context.getCompanyIdentity().activitePrincipale()).isEqualTo("86.90A");
    }

    @Test
    void nonEmployerWithHintsFallsBackToHeuristics() {
        when(client.search(anyString(), anyString())).thenReturn(List.of(assistanceAmbulance()
            .trancheEffectifSalarie("NN")
            .nombreEtablissementsOuverts(1)
            .build()));

        EnrichmentContext context = contextFor("Assistance Ambulance");
        context.addSignal(SignalType.RECRUITING_DETECTED, SignalImportance.HIGH, "website", null);
        enricher.enrich(context);

        assertThat(context.getSizeEstimate().bucket()).isEqualTo(SizeBucket.FROM_2_TO_5);
        assertThat(context.getSizeEstimate().confidence()).isEqualTo(SizeConfidence.LOW);
        assertThat(context.getSizeEstimate().reasons()).extracting(SizeEstimate.Reason::key)
            .containsExactly("HEURISTIC_RECRUITING");
    }

    @Test
    void severalHintsRaiseTheBucketAndConfidence() {
        when(client.search(anyString(), anyString())).thenReturn(List.of(assistanceAmbulance()
            .trancheEffectifSalarie("NN")
            .nombreEtablissementsOuverts(1)
            .build()));

        EnrichmentContext context = contextFor("Assistance Ambulance");
        context.addSignal(SignalType.RECRUITING_DETECTED, SignalImportance.HIGH, "website", null);
        context.addSignal(SignalType.FLEET_DETECTED, SignalImportance.HIGH, "website", null);
        enricher.enrich(context);

        assertThat(context.getSizeEstimate().bucket()).isEqualTo(SizeBucket.FROM_6_TO_9);
        assertThat(context.getSizeEstimate().confidence()).isEqualTo(SizeConfidence.MEDIUM);
    }

    @Test
    void nonEmployerWithoutHintIsSoloWithLowConfidence() {
        when(client.search(anyString(), anyString())).thenReturn(List.of(assistanceAmbulance()
            .trancheEffectifSalarie("NN")
            .nombreEtablissementsOuverts(1)
            .build()));

        EnrichmentContext context = contextFor("Assistance Ambulance");
        enricher.enrich(context);

        assertThat(context.getSizeEstimate().bucket()).isEqualTo(SizeBucket.SOLO);
        assertThat(context.getSizeEstimate().confidence()).isEqualTo(SizeConfidence.LOW);
        assertThat(context.getSizeEstimate().reasons()).extracting(SizeEstimate.Reason::key)
            .containsExactly("SIRENE_NON_EMPLOYER");
    }

    @Test
    void noRegistryMatchAndNoHintYieldsNoEstimate() {
        when(client.search(anyString(), anyString())).thenReturn(List.of());

        EnrichmentContext context = contextFor("Ambulances Inconnues");
        enricher.enrich(context);

        // We say nothing rather than guess SOLO.
        assertThat(context.getSizeEstimate()).isNull();
        assertThat(context.getCompanyIdentity()).isNull();
        assertThat(context.getSiren()).isNull();
    }

    @Test
    void addressWithoutPostalCodeSkipsTheApiEntirely() {
        EnrichmentContext context = EnrichmentContext.builder()
            .mode(EnrichmentMode.FULL)
            .name("Berlin Ambulanz")
            .address("Hauptstrasse 4, Berlin, Germany")
            .build();

        enricher.enrich(context);

        verify(client, never()).search(anyString(), anyString());
        assertThat(context.getSizeEstimate()).isNull();
    }

    @Test
    void apiDownDegradesToHeuristicsWithoutThrowing() {
        when(client.search(anyString(), anyString())).thenReturn(List.of());

        EnrichmentContext context = contextFor("Assistance Ambulance");
        context.addSignal(SignalType.MANY_REVIEWS, SignalImportance.HIGH, "google-places", null);
        context.addSignal(SignalType.FLEET_DETECTED, SignalImportance.HIGH, "website", null);

        enricher.enrich(context);

        assertThat(context.getSizeEstimate().bucket()).isEqualTo(SizeBucket.FROM_6_TO_9);
        assertThat(context.getSizeEstimate().confidence()).isEqualTo(SizeConfidence.MEDIUM);
    }
}
