package com.kerflowapp.kerflow.services.enrichment;

/**
 * A single enrichment source (Google data, website scrape, tomorrow Instagram, SIRENE...).
 * Adding a new connector = one @Component implementing this interface; the engine picks it up.
 */
public interface Enricher {

    /**
     * Stable identifier, used as {@code ProspectSignal.source} (e.g. "google-places", "website").
     */
    String id();

    /**
     * Execution order (lower runs first).
     */
    int order();

    /**
     * Whether this enricher should run at all for the given context. Lets an enricher stay
     * out of the runs where it would add latency or duplicate work (see {@link EnrichmentMode}).
     */
    default boolean supports(EnrichmentContext context) {
        return true;
    }

    /**
     * Reads the context and accumulates data + signals on it.
     */
    void enrich(EnrichmentContext context);

}
