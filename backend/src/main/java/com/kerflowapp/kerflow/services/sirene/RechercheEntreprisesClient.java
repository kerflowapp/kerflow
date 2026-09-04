package com.kerflowapp.kerflow.services.sirene;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Client for the French government's recherche-entreprises API: free, no key, no auth,
 * rate-limited to 7 req/s (fine — we only call it on prospect creation and on explicit
 * enrichment, never during a Google search).
 * <p>
 * Never throws: any failure returns an empty list so enrichment degrades to heuristics.
 */
@Slf4j
@Service
public class RechercheEntreprisesClient {

    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);
    private static final int PER_PAGE = 5;

    /**
     * The API returns closed companies too; they are never valid prospects.
     */
    private static final String ETAT_ACTIF = "A";

    private final String baseUrl;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public RechercheEntreprisesClient(
        @Value("${sirene.recherche-entreprises.base-url:https://recherche-entreprises.api.gouv.fr}") String baseUrl,
        ObjectMapper objectMapper) {

        this.baseUrl = baseUrl;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    }

    /**
     * Searches active companies by name, narrowed to a postal code to keep homonyms out.
     */
    public List<CompanyIdentity> search(String name, String postalCode) {
        if (name == null || name.isBlank() || postalCode == null || postalCode.isBlank()) {
            return List.of();
        }

        String url = baseUrl + "/search"
            + "?q=" + URLEncoder.encode(name, StandardCharsets.UTF_8)
            + "&code_postal=" + URLEncoder.encode(postalCode, StandardCharsets.UTF_8)
            + "&per_page=" + PER_PAGE;

        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(REQUEST_TIMEOUT)
                .GET()
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                LOGGER.warn("recherche-entreprises search failed: {} {}", response.statusCode(), response.body());
                return List.of();
            }

            return parse(response.body());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.warn("recherche-entreprises search interrupted for '{}'", name);
            return List.of();
        } catch (Exception e) {
            LOGGER.warn("recherche-entreprises search errored for '{}': {}", name, e.getMessage());
            return List.of();
        }
    }

    List<CompanyIdentity> parse(String body) {
        JsonNode root = objectMapper.readTree(body);
        List<CompanyIdentity> companies = new ArrayList<>();

        for (JsonNode result : root.path("results")) {
            String etat = text(result, "etat_administratif");
            if (!ETAT_ACTIF.equals(etat)) {
                continue;
            }

            JsonNode siege = result.path("siege");
            JsonNode complements = result.path("complements");

            companies.add(CompanyIdentity.builder()
                .siren(text(result, "siren"))
                .nomComplet(text(result, "nom_complet"))
                .nomRaisonSociale(text(result, "nom_raison_sociale"))
                .sigle(text(result, "sigle"))
                .listeEnseignes(strings(siege.path("liste_enseignes")))
                .nomCommercial(text(siege, "nom_commercial"))
                .codePostalSiege(text(siege, "code_postal"))
                .trancheEffectifSalarie(text(result, "tranche_effectif_salarie"))
                .anneeTrancheEffectifSalarie(integer(result, "annee_tranche_effectif_salarie"))
                .dateCreation(date(result, "date_creation"))
                .nombreEtablissementsOuverts(integer(result, "nombre_etablissements_ouverts"))
                .activitePrincipale(text(result, "activite_principale"))
                .sectionActivitePrincipale(text(result, "section_activite_principale"))
                .etatAdministratif(etat)
                .estEntrepreneurIndividuel(complements.path("est_entrepreneur_individuel").asBoolean(false))
                .estAssociation(complements.path("est_association").asBoolean(false))
                .build());
        }

        return companies;
    }

    private static String text(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isMissingNode() || value.isNull() ? null : value.asText();
    }

    private static Integer integer(JsonNode node, String field) {
        String value = text(node, field);
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.valueOf(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static LocalDate date(JsonNode node, String field) {
        String value = text(node, field);
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private static List<String> strings(JsonNode arrayNode) {
        if (!arrayNode.isArray()) {
            return List.of();
        }
        List<String> values = new ArrayList<>();
        for (JsonNode item : arrayNode) {
            if (!item.isNull()) {
                values.add(item.asText());
            }
        }
        return values;
    }
}
