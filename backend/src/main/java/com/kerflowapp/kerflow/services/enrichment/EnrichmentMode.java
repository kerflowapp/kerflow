package com.kerflowapp.kerflow.services.enrichment;

/**
 * What an enrichment run is for, so enrichers can opt out of the runs they would only
 * slow down or duplicate.
 */
public enum EnrichmentMode {

    /**
     * Live Google search: latency-critical and run on every result. Registry lookups are
     * excluded here — the search must never call an external rate-limited API per result.
     */
    SEARCH,

    /**
     * Prospect creation: the Google and website signals already came from the search, so
     * only the enrichers that were skipped during SEARCH run here.
     */
    CREATION,

    /**
     * Explicit re-enrichment: everything runs.
     */
    FULL

}
