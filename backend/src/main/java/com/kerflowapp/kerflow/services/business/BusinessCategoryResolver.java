package com.kerflowapp.kerflow.services.business;

import com.kerflowapp.kerflow.domain.enums.BusinessCategory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Resolves what a company does, from its NAF code first and its Google types as a
 * fallback. Deliberately a curated ~25-rule table rather than the full 732-entry NAF
 * nomenclature: the categories that matter for the current persona are named, everything
 * else is honestly reported as OTHER.
 */
@Component
public class BusinessCategoryResolver {

    /**
     * NAF rev.2 shape, e.g. "86.90A". Legacy rev.1 codes ("85.1J") still come out of the
     * registry, and their declared section is wrong (an ambulance reported under
     * "Education"), so anything not matching this is not interpreted at all.
     */
    private static final Pattern NAF_REV2 = Pattern.compile("^\\d{2}\\.\\d{2}[A-Z]$");

    /**
     * Prefixes on the code without its dot, longest match wins. Short prefixes ("41", "42",
     * "43" = the whole construction section, "812" = cleaning) are what keeps this table
     * small while still covering every craftsman.
     */
    private static final Map<String, BusinessCategory> NAF_PREFIXES = buildNafPrefixes();

    /**
     * Google's legacy type vocabulary is small and consumer-facing. It resolves the obvious
     * trades and nothing else: "hospital"/"doctor" are NOT mapped to AMBULANCE (a clinic is
     * not an ambulance company), "school" is not mapped to DRIVING_SCHOOL.
     */
    private static final Map<String, BusinessCategory> GOOGLE_TYPES = Map.ofEntries(
        Map.entry("taxi_stand", BusinessCategory.TAXI_VTC),
        Map.entry("moving_company", BusinessCategory.ROAD_FREIGHT),
        Map.entry("storage", BusinessCategory.ROAD_FREIGHT),
        Map.entry("car_rental", BusinessCategory.VEHICLE_RENTAL),
        Map.entry("car_repair", BusinessCategory.AUTO_SERVICES),
        Map.entry("car_dealer", BusinessCategory.AUTO_SERVICES),
        Map.entry("car_wash", BusinessCategory.AUTO_SERVICES),
        Map.entry("plumber", BusinessCategory.CONSTRUCTION),
        Map.entry("electrician", BusinessCategory.CONSTRUCTION),
        Map.entry("roofing_contractor", BusinessCategory.CONSTRUCTION),
        Map.entry("painter", BusinessCategory.CONSTRUCTION),
        Map.entry("general_contractor", BusinessCategory.CONSTRUCTION)
    );

    private static Map<String, BusinessCategory> buildNafPrefixes() {
        Map<String, BusinessCategory> prefixes = new LinkedHashMap<>();

        // Longest prefixes first: resolution walks this map in order.
        prefixes.put("4932Z", BusinessCategory.TAXI_VTC);
        prefixes.put("8690A", BusinessCategory.AMBULANCE);

        prefixes.put("4941A", BusinessCategory.ROAD_FREIGHT);
        prefixes.put("4941B", BusinessCategory.ROAD_FREIGHT);
        prefixes.put("4941C", BusinessCategory.ROAD_FREIGHT);
        prefixes.put("4942Z", BusinessCategory.ROAD_FREIGHT);
        prefixes.put("4920Z", BusinessCategory.ROAD_FREIGHT);
        prefixes.put("5320Z", BusinessCategory.ROAD_FREIGHT);

        prefixes.put("4931Z", BusinessCategory.PASSENGER_TRANSPORT);
        prefixes.put("4939A", BusinessCategory.PASSENGER_TRANSPORT);
        prefixes.put("4939B", BusinessCategory.PASSENGER_TRANSPORT);
        prefixes.put("4910Z", BusinessCategory.PASSENGER_TRANSPORT);

        prefixes.put("7711A", BusinessCategory.VEHICLE_RENTAL);
        prefixes.put("7711B", BusinessCategory.VEHICLE_RENTAL);
        prefixes.put("7712Z", BusinessCategory.VEHICLE_RENTAL);

        prefixes.put("4511Z", BusinessCategory.AUTO_SERVICES);
        prefixes.put("4519Z", BusinessCategory.AUTO_SERVICES);
        prefixes.put("4520A", BusinessCategory.AUTO_SERVICES);
        prefixes.put("4520B", BusinessCategory.AUTO_SERVICES);
        prefixes.put("4532Z", BusinessCategory.AUTO_SERVICES);
        prefixes.put("4540Z", BusinessCategory.AUTO_SERVICES);

        prefixes.put("8553Z", BusinessCategory.DRIVING_SCHOOL);
        prefixes.put("8130Z", BusinessCategory.LANDSCAPING);

        prefixes.put("812", BusinessCategory.CLEANING);

        // Whole construction section (F): structural work, civil engineering, all trades.
        prefixes.put("41", BusinessCategory.CONSTRUCTION);
        prefixes.put("42", BusinessCategory.CONSTRUCTION);
        prefixes.put("43", BusinessCategory.CONSTRUCTION);

        return prefixes;
    }

    /**
     * True when the code can be interpreted at all (rev.2 shape). A legacy code is still
     * persisted for traceability, but yields no category and no section.
     */
    public boolean isInterpretableNaf(String nafCode) {
        return nafCode != null && NAF_REV2.matcher(nafCode.trim().toUpperCase(Locale.ROOT)).matches();
    }

    /**
     * Resolves the category from a NAF code. Empty when the code is unusable; OTHER when
     * it is valid but outside the curated table.
     */
    public Optional<BusinessCategory> resolveFromNaf(String nafCode) {
        if (!isInterpretableNaf(nafCode)) {
            return Optional.empty();
        }

        String normalized = nafCode.trim().toUpperCase(Locale.ROOT).replace(".", "");
        for (Map.Entry<String, BusinessCategory> entry : NAF_PREFIXES.entrySet()) {
            if (normalized.startsWith(entry.getKey())) {
                return Optional.of(entry.getValue());
            }
        }
        return Optional.of(BusinessCategory.OTHER);
    }

    /**
     * Resolves the category from Google types, used only when NAF gave nothing usable.
     * Returns the first mapped type: generic types ("establishment", "point_of_interest")
     * are never mapped, so they resolve to nothing rather than to a wrong guess.
     */
    public Optional<GoogleTypeMatch> resolveFromGoogleTypes(List<String> googleTypes) {
        if (googleTypes == null) {
            return Optional.empty();
        }
        for (String type : googleTypes) {
            if (type == null) {
                continue;
            }
            BusinessCategory category = GOOGLE_TYPES.get(type.trim().toLowerCase(Locale.ROOT));
            if (category != null) {
                return Optional.of(new GoogleTypeMatch(category, type));
            }
        }
        return Optional.empty();
    }

    /**
     * The matched category plus the Google type it came from, so the frontend can name it.
     */
    public record GoogleTypeMatch(BusinessCategory category, String googleType) {
    }
}
