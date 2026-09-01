package com.kerflowapp.kerflow.services.sirene;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Matches a Google Business name against registry candidates.
 * <p>
 * Biased towards saying "no": a wrong SIREN would attach a wrong size, a wrong activity
 * and a wrong age to the prospect, which is worse than knowing nothing. Hence: when two
 * distinct companies both look like a match, we return nothing.
 */
@Slf4j
@Component
public class CompanyNameMatcher {

    private static final Pattern POSTAL_CODE = Pattern.compile("\\b\\d{5}\\b");
    private static final Pattern NON_ALPHANUMERIC = Pattern.compile("[^a-z0-9]+");
    private static final Pattern DIACRITICS = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

    /**
     * Legal forms carry no identity: "SARL Martin" and "Martin" are the same company.
     */
    private static final Set<String> LEGAL_FORMS = Set.of(
        "sarl", "sas", "sasu", "eurl", "sci", "sa", "snc", "scop", "selarl",
        "sarlu", "earl", "gie", "sem", "scm", "scp", "ei", "eirl", "asso", "association"
    );

    private static final int MIN_TOKEN_LENGTH = 3;

    /**
     * Extracts the French postal code from a Google formatted address. No postal code
     * means no registry lookup at all (foreign address, incomplete listing).
     */
    public Optional<String> extractPostalCode(String address) {
        if (address == null || address.isBlank()) {
            return Optional.empty();
        }
        Matcher matcher = POSTAL_CODE.matcher(address);
        return matcher.find() ? Optional.of(matcher.group()) : Optional.empty();
    }

    /**
     * Returns the single company matching the given name, or empty when there is no match
     * or when several distinct companies match.
     */
    public Optional<CompanyIdentity> match(String googleName, List<CompanyIdentity> candidates) {
        if (googleName == null || googleName.isBlank() || candidates == null || candidates.isEmpty()) {
            return Optional.empty();
        }

        Set<String> googleTokens = tokenize(googleName);
        if (googleTokens.isEmpty()) {
            return Optional.empty();
        }

        List<CompanyIdentity> matches = new ArrayList<>();
        for (CompanyIdentity candidate : candidates) {
            if (matches(googleTokens, candidate)) {
                matches.add(candidate);
            }
        }

        if (matches.isEmpty()) {
            return Optional.empty();
        }

        long distinctSirens = matches.stream()
            .map(CompanyIdentity::siren)
            .distinct()
            .count();

        if (distinctSirens > 1) {
            LOGGER.debug("Ambiguous registry match for '{}': {} distinct companies, skipping", googleName, distinctSirens);
            return Optional.empty();
        }

        return Optional.of(matches.getFirst());
    }

    private boolean matches(Set<String> googleTokens, CompanyIdentity candidate) {
        List<String> names = new ArrayList<>();
        names.add(candidate.nomComplet());
        names.add(candidate.nomRaisonSociale());
        names.add(candidate.sigle());
        names.add(candidate.nomCommercial());
        if (candidate.listeEnseignes() != null) {
            names.addAll(candidate.listeEnseignes());
        }

        for (String name : names) {
            Set<String> candidateTokens = tokenize(name);
            if (candidateTokens.isEmpty()) {
                continue;
            }
            // Equal, or one name's tokens fully contained in the other's: "AMBULANCES BELLOIR"
            // matches "AMBULANCES BELLOIR GUICHEN", but not the unrelated "AMBULANCES DUPONT".
            if (candidateTokens.equals(googleTokens)
                || candidateTokens.containsAll(googleTokens)
                || googleTokens.containsAll(candidateTokens)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Lowercase, strip accents and punctuation, drop legal forms and tokens shorter than
     * three characters (single letters and "de"/"du" carry no identity).
     */
    private Set<String> tokenize(String name) {
        if (name == null || name.isBlank()) {
            return Set.of();
        }

        String normalized = Normalizer.normalize(name.toLowerCase(Locale.FRENCH), Normalizer.Form.NFD);
        normalized = DIACRITICS.matcher(normalized).replaceAll("");
        normalized = normalized.replace('\'', ' ').replace('’', ' ');

        Set<String> tokens = new LinkedHashSet<>();
        for (String token : NON_ALPHANUMERIC.split(normalized)) {
            if (token.length() >= MIN_TOKEN_LENGTH && !LEGAL_FORMS.contains(token)) {
                tokens.add(token);
            }
        }
        return tokens;
    }
}
