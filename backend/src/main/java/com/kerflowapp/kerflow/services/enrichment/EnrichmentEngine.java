package com.kerflowapp.kerflow.services.enrichment;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
public class EnrichmentEngine {

    private final List<Enricher> enrichers;

    public EnrichmentEngine(List<Enricher> enrichers) {
        this.enrichers = enrichers.stream()
            .sorted(Comparator.comparingInt(Enricher::order))
            .toList();
    }

    /**
     * Runs every enricher supporting the context's mode. A failing enricher never prevents
     * the others from running.
     */
    public void run(EnrichmentContext context) {
        for (Enricher enricher : enrichers) {
            try {
                if (!enricher.supports(context)) {
                    continue;
                }
                enricher.enrich(context);
            } catch (Exception e) {
                LOGGER.warn("Enricher {} failed, continuing with the remaining enrichers", enricher.id(), e);
            }
        }
    }

}
