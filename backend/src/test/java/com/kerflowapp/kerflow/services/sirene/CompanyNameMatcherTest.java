package com.kerflowapp.kerflow.services.sirene;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CompanyNameMatcherTest {

    private final CompanyNameMatcher matcher = new CompanyNameMatcher();

    private CompanyIdentity company(String siren, String nomComplet) {
        return CompanyIdentity.builder().siren(siren).nomComplet(nomComplet).build();
    }

    @Test
    void extractsFrenchPostalCode() {
        assertThat(matcher.extractPostalCode("17 rue Madeleine Brès, 35580 Guichen, France")).contains("35580");
        assertThat(matcher.extractPostalCode("1 Infinite Loop, Cupertino, CA 95014, USA")).contains("95014");
    }

    @Test
    void noPostalCodeWhenAddressHasNone() {
        assertThat(matcher.extractPostalCode("Hauptstrasse 4, Berlin, Germany")).isEmpty();
        assertThat(matcher.extractPostalCode(null)).isEmpty();
        assertThat(matcher.extractPostalCode("")).isEmpty();
    }

    @Test
    void matchesIgnoringLegalForms() {
        List<CompanyIdentity> candidates = List.of(company("123456789", "MARTIN"));

        assertThat(matcher.match("SARL Martin", candidates)).contains(candidates.getFirst());
        assertThat(matcher.match("Martin SAS", candidates)).contains(candidates.getFirst());
    }

    @Test
    void matchesIgnoringAccentsAndPunctuation() {
        List<CompanyIdentity> candidates = List.of(company("123456789", "AMBULANCES DU CENTRE"));

        assertThat(matcher.match("Ambulances du Centre", candidates)).contains(candidates.getFirst());
        assertThat(matcher.match("Ambulances-du-Centre", candidates)).contains(candidates.getFirst());
        assertThat(matcher.match("Café Crème", List.of(company("1", "CAFE CREME")))).isPresent();
    }

    @Test
    void matchesOnEnseigneOrCommercialName() {
        CompanyIdentity candidate = CompanyIdentity.builder()
            .siren("123456789")
            .nomComplet("JOLLY ET FILS")
            .listeEnseignes(List.of("AMBULANCE CHANTENAYSIENNE"))
            .build();

        assertThat(matcher.match("Ambulance Chantenaysienne", List.of(candidate))).contains(candidate);
    }

    @Test
    void doesNotMatchUnrelatedName() {
        List<CompanyIdentity> candidates = List.of(company("123456789", "AMBULANCES DUPONT"));

        assertThat(matcher.match("Ambulances Belloir", candidates)).isEmpty();
    }

    @Test
    void ambiguousMatchYieldsNothing() {
        // Two distinct companies both look right: a wrong SIREN would attach a wrong size,
        // activity and age to the prospect, so we prefer knowing nothing.
        List<CompanyIdentity> candidates = List.of(
            company("111111111", "MARTIN"),
            company("222222222", "SARL MARTIN")
        );

        assertThat(matcher.match("Martin", candidates)).isEmpty();
    }

    @Test
    void sameCompanyListedTwiceStillMatches() {
        List<CompanyIdentity> candidates = List.of(
            company("111111111", "MARTIN"),
            company("111111111", "SARL MARTIN")
        );

        assertThat(matcher.match("Martin", candidates)).isPresent();
    }

    @Test
    void emptyInputsYieldNothing() {
        assertThat(matcher.match("Martin", List.of())).isEmpty();
        assertThat(matcher.match("Martin", null)).isEmpty();
        assertThat(matcher.match(null, List.of(company("1", "MARTIN")))).isEmpty();
        assertThat(matcher.match("", List.of(company("1", "MARTIN")))).isEmpty();
    }
}
