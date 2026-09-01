package com.kerflowapp.kerflow.services.enrichment;

import com.kerflowapp.kerflow.domain.BusinessProfile;
import com.kerflowapp.kerflow.domain.ProspectSignal;
import com.kerflowapp.kerflow.domain.SizeEstimate;
import com.kerflowapp.kerflow.domain.enums.SignalImportance;
import com.kerflowapp.kerflow.domain.enums.SignalType;
import com.kerflowapp.kerflow.services.search.WebsiteScraperService.WebsiteAudit;
import com.kerflowapp.kerflow.services.sirene.CompanyIdentity;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Mutable snapshot shared by the enrichers of a single run: input data about the
 * business, plus everything the enrichers accumulate (website audit, registry identity,
 * signals, size estimate, business profile).
 */
@Getter
@Setter
@Builder
public class EnrichmentContext {

    @Builder.Default
    private EnrichmentMode mode = EnrichmentMode.FULL;

    private String name;
    private String address;
    private String phone;
    private String website;
    private String googlePlaceId;
    private Double rating;
    private Integer userRatingsTotal;

    @Builder.Default
    private List<String> googleTypes = new ArrayList<>();

    /**
     * Set by the first enricher that fetches the website, so later enrichers reuse the same fetch.
     */
    private WebsiteAudit websiteAudit;

    /**
     * Set by the enricher that queries the company registry, so later enrichers reuse the
     * same lookup instead of calling the API again.
     */
    private CompanyIdentity companyIdentity;

    private String siren;
    private SizeEstimate sizeEstimate;
    private BusinessProfile businessProfile;

    @Builder.Default
    private List<ProspectSignal> signals = new ArrayList<>();

    public void addSignal(SignalType type, SignalImportance importance, String source, Map<String, Object> params) {
        signals.add(ProspectSignal.of(type, importance, source, params));
    }

    public boolean hasSignal(SignalType type) {
        return signals.stream().anyMatch(signal -> signal.type() == type);
    }

}
