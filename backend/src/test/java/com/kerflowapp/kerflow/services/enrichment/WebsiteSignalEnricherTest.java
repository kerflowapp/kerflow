package com.kerflowapp.kerflow.services.enrichment;

import com.kerflowapp.kerflow.domain.ProspectSignal;
import com.kerflowapp.kerflow.domain.enums.SignalType;
import com.kerflowapp.kerflow.services.search.WebsiteScraperService.SocialLinks;
import com.kerflowapp.kerflow.services.search.WebsiteScraperService.WebsiteAudit;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WebsiteSignalEnricherTest {

    // The scraper is never called: every test puts an audit in the context beforehand.
    private final WebsiteSignalEnricher enricher = new WebsiteSignalEnricher(null);

    private EnrichmentContext contextWithAudit(WebsiteAudit audit) {
        EnrichmentContext context = EnrichmentContext.builder()
            .website("https://example.fr")
            .build();
        context.setWebsiteAudit(audit);
        return context;
    }

    private List<SignalType> enrich(EnrichmentContext context) {
        enricher.enrich(context);
        return context.getSignals().stream().map(ProspectSignal::type).toList();
    }

    @Test
    void httpsSiteYieldsProfessionalWebsiteInsteadOfNoHttps() {
        WebsiteAudit audit = WebsiteAudit.builder()
            .finalUrl("https://example.fr")
            .https(true)
            .build();

        List<SignalType> types = enrich(contextWithAudit(audit));

        assertThat(types).contains(SignalType.PROFESSIONAL_WEBSITE);
        assertThat(types).doesNotContain(SignalType.NO_HTTPS);
    }

    @Test
    void nonHttpsSiteYieldsNoHttpsInsteadOfProfessionalWebsite() {
        WebsiteAudit audit = WebsiteAudit.builder()
            .finalUrl("http://example.fr")
            .https(false)
            .build();

        List<SignalType> types = enrich(contextWithAudit(audit));

        assertThat(types).contains(SignalType.NO_HTTPS);
        assertThat(types).doesNotContain(SignalType.PROFESSIONAL_WEBSITE);
    }

    @Test
    void socialLinksYieldSocialMediaPresenceWithCount() {
        WebsiteAudit audit = WebsiteAudit.builder()
            .finalUrl("https://example.fr")
            .https(true)
            .socialLinks(SocialLinks.builder()
                .facebook("https://facebook.com/example")
                .instagram("https://instagram.com/example")
                .build())
            .build();

        EnrichmentContext context = contextWithAudit(audit);
        List<SignalType> types = enrich(context);

        assertThat(types).contains(SignalType.SOCIAL_MEDIA_PRESENCE);
        ProspectSignal social = context.getSignals().stream()
            .filter(s -> s.type() == SignalType.SOCIAL_MEDIA_PRESENCE)
            .findFirst().orElseThrow();
        assertThat(social.params()).containsEntry("count", 2);
    }

    @Test
    void noSocialLinksYieldNoSocialMediaPresence() {
        WebsiteAudit audit = WebsiteAudit.builder()
            .finalUrl("https://example.fr")
            .https(true)
            .socialLinks(SocialLinks.empty())
            .build();

        assertThat(enrich(contextWithAudit(audit))).doesNotContain(SignalType.SOCIAL_MEDIA_PRESENCE);
    }

    @Test
    void failedFetchEmitsNoPositiveSignals() {
        assertThat(enrich(contextWithAudit(WebsiteAudit.empty()))).isEmpty();
    }

    @Test
    void missingWebsiteStillYieldsNoWebsite() {
        EnrichmentContext context = EnrichmentContext.builder().build();

        assertThat(enrich(context)).containsExactly(SignalType.NO_WEBSITE);
    }
}
