package com.kerflowapp.kerflow.services.sirene;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RechercheEntreprisesClientTest {

    private final RechercheEntreprisesClient client =
        new RechercheEntreprisesClient("https://recherche-entreprises.api.gouv.fr", new ObjectMapper());

    private String fixture() throws Exception {
        try (var stream = getClass().getResourceAsStream("/sirene/recherche-entreprises-ambulances-44000.json")) {
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    @Test
    void parsesRealResponse() throws Exception {
        List<CompanyIdentity> companies = client.parse(fixture());

        CompanyIdentity assistanceAmbulance = companies.stream()
            .filter(c -> "402828909".equals(c.siren()))
            .findFirst()
            .orElseThrow();

        assertThat(assistanceAmbulance.nomComplet()).isEqualTo("ASSISTANCE AMBULANCE");
        assertThat(assistanceAmbulance.activitePrincipale()).isEqualTo("86.90A");
        assertThat(assistanceAmbulance.sectionActivitePrincipale()).isEqualTo("Q");
        assertThat(assistanceAmbulance.dateCreation()).isEqualTo(LocalDate.of(1995, 10, 17));
        assertThat(assistanceAmbulance.trancheEffectifSalarie()).isEqualTo("12");
        assertThat(assistanceAmbulance.anneeTrancheEffectifSalarie()).isEqualTo(2023);
        assertThat(assistanceAmbulance.nombreEtablissementsOuverts()).isEqualTo(2);
        assertThat(assistanceAmbulance.etatAdministratif()).isEqualTo("A");

        // Searching code_postal=44000 matches companies with *an establishment* there, while
        // codePostalSiege is the head office's — 44400 here. Never assume they are equal.
        assertThat(assistanceAmbulance.codePostalSiege()).isEqualTo("44400");
    }

    @Test
    void filtersOutClosedCompanies() throws Exception {
        List<CompanyIdentity> companies = client.parse(fixture());

        // The fixture holds a real closed company ("AMBULANCE AGREE AMBULANCE NANTAISE",
        // etat_administratif = C). The API returns those; they are never valid prospects.
        assertThat(companies).extracting(CompanyIdentity::siren).doesNotContain("338901226");
        assertThat(companies).allMatch(company -> "A".equals(company.etatAdministratif()));
        assertThat(companies).hasSize(2);
    }

    @Test
    void parsesCompanyDeclaringNoWorkforce() throws Exception {
        List<CompanyIdentity> companies = client.parse(fixture());

        CompanyIdentity nonEmployer = companies.stream()
            .filter(c -> "340222306".equals(c.siren()))
            .findFirst()
            .orElseThrow();

        // "NN" is very common for small businesses: it means unknown, not nobody.
        assertThat(nonEmployer.trancheEffectifSalarie()).isEqualTo("NN");
        assertThat(nonEmployer.nombreEtablissementsOuverts()).isEqualTo(5);
    }

    @Test
    void emptyBodyYieldsNoCompany() throws Exception {
        assertThat(client.parse("{\"results\":[]}")).isEmpty();
        assertThat(client.parse("{}")).isEmpty();
    }

    @Test
    void blankInputsSkipTheCallEntirely() {
        assertThat(client.search(null, "44000")).isEmpty();
        assertThat(client.search("Ambulance", null)).isEmpty();
        assertThat(client.search("  ", "44000")).isEmpty();
    }

    @Test
    void unreachableApiNeverThrows() {
        RechercheEntreprisesClient offline =
            new RechercheEntreprisesClient("http://localhost:1", new ObjectMapper());

        assertThat(offline.search("Ambulance", "44000")).isEmpty();
    }
}
