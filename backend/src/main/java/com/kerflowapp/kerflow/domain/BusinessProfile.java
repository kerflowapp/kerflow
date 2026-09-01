package com.kerflowapp.kerflow.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.kerflowapp.kerflow.domain.enums.BusinessCategory;
import com.kerflowapp.kerflow.domain.enums.BusinessCategorySource;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Deterministic business profile of a prospect (persisted as jsonb): what the company
 * does, sourced and auditable. Like signals and size estimates, only facts and keys are
 * stored: the frontend translates each fact key (business.facts.KEY) with its params.
 * <p>
 * Coexists with {@link ProspectAnalysis} without ever merging into it: this one is
 * computed and traceable, the analysis is an agent's interpretation.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record BusinessProfile(
    BusinessCategory category,
    BusinessCategorySource categorySource,
    String nafCode,
    String nafSection,
    List<String> googleTypes,
    List<Fact> facts,
    Instant profiledAt
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Fact(String key, Map<String, Object> params) {

        public static Fact of(String key, Map<String, Object> params) {
            return new Fact(key, params);
        }
    }

    public static BusinessProfile of(BusinessCategory category,
                                     BusinessCategorySource categorySource,
                                     String nafCode,
                                     String nafSection,
                                     List<String> googleTypes,
                                     List<Fact> facts) {
        
        return new BusinessProfile(category, categorySource, nafCode, nafSection, googleTypes, facts, Instant.now());
    }

}
