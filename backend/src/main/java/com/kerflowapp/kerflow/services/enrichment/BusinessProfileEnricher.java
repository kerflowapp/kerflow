package com.kerflowapp.kerflow.services.enrichment;

import com.kerflowapp.kerflow.domain.BusinessProfile;
import com.kerflowapp.kerflow.domain.BusinessProfile.Fact;
import com.kerflowapp.kerflow.domain.enums.BusinessCategory;
import com.kerflowapp.kerflow.domain.enums.BusinessCategorySource;
import com.kerflowapp.kerflow.services.business.BusinessCategoryResolver;
import com.kerflowapp.kerflow.services.business.BusinessCategoryResolver.GoogleTypeMatch;
import com.kerflowapp.kerflow.services.sirene.CompanyIdentity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Builds the deterministic business profile: what the company does, from the registry's
 * NAF code first and the Google listing as a fallback.
 * <p>
 * Makes no HTTP call: it reuses the registry lookup {@link CompanySizeEnricher} already
 * put on the context, which is why it still runs when that lookup found nothing.
 * <p>
 * Emits no signal on purpose: a category is a matching dimension, not a quality, and
 * weighting it per target business is roadmap phase 7.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BusinessProfileEnricher implements Enricher {

    private static final String ID = "business";

    /**
     * Below this, "présente depuis X ans" would oversell a company that just started.
     */
    private static final int RECENT_COMPANY_YEARS = 3;

    private final BusinessCategoryResolver businessCategoryResolver;

    @Override
    public String id() {
        return ID;
    }

    @Override
    public int order() {
        return 40;
    }

    @Override
    public boolean supports(EnrichmentContext context) {
        return context.getMode() != EnrichmentMode.SEARCH;
    }

    @Override
    public void enrich(EnrichmentContext context) {
        CompanyIdentity company = context.getCompanyIdentity();
        String nafCode = company != null ? company.activitePrincipale() : null;

        List<Fact> facts = new ArrayList<>();
        BusinessCategory category = null;
        BusinessCategorySource source = null;

        Optional<BusinessCategory> fromNaf = businessCategoryResolver.resolveFromNaf(nafCode);
        if (fromNaf.isPresent()) {
            category = fromNaf.get();
            source = BusinessCategorySource.NAF;
            facts.add(category == BusinessCategory.OTHER
                ? Fact.of("ACTIVITY_NAF_UNMAPPED", Map.of("nafCode", nafCode))
                : Fact.of("ACTIVITY_NAF", Map.of("nafCode", nafCode)));
        }

        // Google only speaks when the registry said nothing usable: a legacy NAF code, no
        // match at all, or an activity outside our curated table.
        if (category == null || category == BusinessCategory.OTHER) {
            Optional<GoogleTypeMatch> fromGoogle = businessCategoryResolver.resolveFromGoogleTypes(context.getGoogleTypes());
            if (fromGoogle.isPresent()) {
                GoogleTypeMatch match = fromGoogle.get();
                category = match.category();
                source = BusinessCategorySource.GOOGLE_TYPES;
                facts.add(Fact.of("ACTIVITY_GOOGLE", Map.of("googleType", match.googleType())));
            }
        }

        if (company != null) {
            addAgeFact(facts, company.dateCreation());
            addLegalFormFacts(facts, company);
        }

        if (category != null && category.isFleetLikely()) {
            facts.add(Fact.of("LIKELY_FLEET", Map.of()));
        }

        // An empty shell helps nobody: no category and no fact means we know nothing.
        if (facts.isEmpty() && (category == null || category == BusinessCategory.OTHER)) {
            context.setBusinessProfile(null);
            return;
        }

        // A legacy or missing NAF still gets persisted for traceability, but its declared
        // section is unreliable (an ambulance filed under "Education"), so it is dropped.
        String nafSection = businessCategoryResolver.isInterpretableNaf(nafCode) && company != null
            ? company.sectionActivitePrincipale()
            : null;

        context.setBusinessProfile(BusinessProfile.of(
            category != null ? category : BusinessCategory.OTHER,
            source,
            nafCode,
            nafSection,
            context.getGoogleTypes(),
            facts
        ));
    }

    private void addAgeFact(List<Fact> facts, LocalDate creationDate) {
        if (creationDate == null) {
            return;
        }
        int years = Period.between(creationDate, LocalDate.now()).getYears();
        int year = creationDate.getYear();

        facts.add(years >= RECENT_COMPANY_YEARS
            ? Fact.of("COMPANY_AGE", Map.of("years", years, "year", year))
            : Fact.of("COMPANY_RECENT", Map.of("years", years, "year", year)));
    }

    private void addLegalFormFacts(List<Fact> facts, CompanyIdentity company) {
        if (company.estEntrepreneurIndividuel()) {
            facts.add(Fact.of("LEGAL_FORM_INDIVIDUAL", Map.of()));
        }
        if (company.estAssociation()) {
            facts.add(Fact.of("LEGAL_FORM_ASSOCIATION", Map.of()));
        }
    }
}
