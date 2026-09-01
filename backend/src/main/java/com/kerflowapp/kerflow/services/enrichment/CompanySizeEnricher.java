package com.kerflowapp.kerflow.services.enrichment;

import com.kerflowapp.kerflow.domain.SizeEstimate;
import com.kerflowapp.kerflow.domain.SizeEstimate.Reason;
import com.kerflowapp.kerflow.domain.enums.SignalImportance;
import com.kerflowapp.kerflow.domain.enums.SignalType;
import com.kerflowapp.kerflow.domain.enums.SizeBucket;
import com.kerflowapp.kerflow.domain.enums.SizeConfidence;
import com.kerflowapp.kerflow.services.sirene.CompanyIdentity;
import com.kerflowapp.kerflow.services.sirene.CompanyNameMatcher;
import com.kerflowapp.kerflow.services.sirene.RechercheEntreprisesClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.Period;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Estimates company size from the French company registry, falling back to heuristics
 * when the registry is silent. Also the single place that queries the registry: it puts
 * the matched company on the context so later enrichers reuse the lookup.
 * <p>
 * Never runs during a Google search: one external call per result would add latency and
 * blow through the API's 7 req/s.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CompanySizeEnricher implements Enricher {

    private static final String ID = "sirene";

    /**
     * INSEE code for a company declaring no workforce. Very common for small businesses,
     * so it means "unknown", not "nobody works here".
     */
    private static final String BRACKET_NON_EMPLOYER = "NN";

    /**
     * A workforce bracket older than this is still useful, but no longer trustworthy enough
     * to claim high confidence.
     */
    private static final int STALE_BRACKET_YEARS = 4;

    private static final int MATURE_COMPANY_YEARS = 5;

    private final RechercheEntreprisesClient rechercheEntreprisesClient;
    private final CompanyNameMatcher companyNameMatcher;

    @Override
    public String id() {
        return ID;
    }

    @Override
    public int order() {
        return 30;
    }

    @Override
    public boolean supports(EnrichmentContext context) {
        return context.getMode() != EnrichmentMode.SEARCH;
    }

    @Override
    public void enrich(EnrichmentContext context) {
        Optional<CompanyIdentity> identity = lookup(context);

        identity.ifPresent(company -> {
            context.setCompanyIdentity(company);
            context.setSiren(company.siren());
            addRegistrySignals(context, company);
        });

        SizeEstimate estimate = identity
            .flatMap(company -> estimateFromBracket(company).or(() -> estimateFromHints(context, company)))
            .orElseGet(() -> estimateFromHints(context, null).orElse(null));

        context.setSizeEstimate(estimate);
    }

    private Optional<CompanyIdentity> lookup(EnrichmentContext context) {
        Optional<String> postalCode = companyNameMatcher.extractPostalCode(context.getAddress());
        if (postalCode.isEmpty()) {
            // No French postal code: a name-only lookup would match homonyms nationwide.
            return Optional.empty();
        }

        List<CompanyIdentity> candidates = rechercheEntreprisesClient.search(context.getName(), postalCode.get());
        return companyNameMatcher.match(context.getName(), candidates);
    }

    private void addRegistrySignals(EnrichmentContext context, CompanyIdentity company) {
        Integer establishments = company.nombreEtablissementsOuverts();
        if (establishments != null && establishments > 1) {
            context.addSignal(SignalType.MULTI_ESTABLISHMENT, SignalImportance.MEDIUM, ID,
                Map.of("count", establishments));
        }

        Integer age = ageInYears(company.dateCreation());
        if (age != null && age > MATURE_COMPANY_YEARS) {
            context.addSignal(SignalType.COMPANY_MATURE, SignalImportance.LOW, ID, Map.of("years", age));
        }
    }

    /**
     * The registry's declared workforce bracket: the only source good enough for HIGH
     * confidence, degraded to MEDIUM when the declaration is old.
     */
    private Optional<SizeEstimate> estimateFromBracket(CompanyIdentity company) {
        String bracket = company.trancheEffectifSalarie();
        if (bracket == null || bracket.isBlank() || BRACKET_NON_EMPLOYER.equals(bracket)) {
            return Optional.empty();
        }

        SizeBucket bucket = toBucket(bracket);
        if (bucket == null) {
            return Optional.empty();
        }

        Integer year = company.anneeTrancheEffectifSalarie();
        SizeConfidence confidence = isStale(year) ? SizeConfidence.MEDIUM : SizeConfidence.HIGH;

        Map<String, Object> params = year != null
            ? Map.of("bracket", bracket, "year", year)
            : Map.of("bracket", bracket);

        return Optional.of(SizeEstimate.of(bucket, confidence,
            List.of(Reason.of("SIRENE_WORKFORCE_BRACKET", params))));
    }

    /**
     * INSEE workforce brackets count employees excluding the owner, so bracket 01 (1-2
     * employees) is 2-3 people in the company — hence FROM_2_TO_5, not SOLO.
     */
    private SizeBucket toBucket(String bracket) {
        return switch (bracket) {
            case "00" -> SizeBucket.SOLO;
            case "01", "02" -> SizeBucket.FROM_2_TO_5;
            case "03" -> SizeBucket.FROM_6_TO_9;
            case "11" -> SizeBucket.FROM_10_TO_19;
            case "12" -> SizeBucket.FROM_20_TO_49;
            case "21", "22", "31", "32", "41", "42", "51", "52", "53" -> SizeBucket.FIFTY_PLUS;
            default -> null;
        };
    }

    private boolean isStale(Integer bracketYear) {
        return bracketYear == null || bracketYear < Year.now().getValue() - STALE_BRACKET_YEARS;
    }

    /**
     * Fallback when the registry declares no workforce: count the hints we already detected
     * elsewhere. Never claims more than MEDIUM — these are inferences, not declarations.
     */
    private Optional<SizeEstimate> estimateFromHints(EnrichmentContext context, CompanyIdentity company) {
        List<Reason> hints = new ArrayList<>();

        if (context.hasSignal(SignalType.RECRUITING_DETECTED)) {
            hints.add(Reason.of("HEURISTIC_RECRUITING", Map.of()));
        }
        if (context.hasSignal(SignalType.FLEET_DETECTED)) {
            hints.add(Reason.of("HEURISTIC_FLEET", Map.of()));
        }
        if (context.hasSignal(SignalType.MANY_REVIEWS)) {
            hints.add(Reason.of("HEURISTIC_MANY_REVIEWS", Map.of()));
        }

        boolean multiEstablishment = company != null
            && company.nombreEtablissementsOuverts() != null
            && company.nombreEtablissementsOuverts() > 1;
        if (multiEstablishment) {
            hints.add(Reason.of("HEURISTIC_MULTI_ESTABLISHMENT",
                Map.of("count", company.nombreEtablissementsOuverts())));
        }

        if (hints.isEmpty()) {
            // Nothing from the registry and no hint: we say nothing rather than guess SOLO.
            if (company == null) {
                return Optional.empty();
            }
            return Optional.of(SizeEstimate.of(SizeBucket.SOLO, SizeConfidence.LOW,
                List.of(Reason.of("SIRENE_NON_EMPLOYER", Map.of()))));
        }

        if (hints.size() == 1 && !multiEstablishment) {
            return Optional.of(SizeEstimate.of(SizeBucket.FROM_2_TO_5, SizeConfidence.LOW, hints));
        }

        return Optional.of(SizeEstimate.of(SizeBucket.FROM_6_TO_9, SizeConfidence.MEDIUM, hints));
    }

    private Integer ageInYears(LocalDate creationDate) {
        if (creationDate == null) {
            return null;
        }
        return Period.between(creationDate, LocalDate.now()).getYears();
    }
}
