package com.kerflowapp.kerflow.services.enrichment;

import com.kerflowapp.kerflow.domain.enums.SignalImportance;
import com.kerflowapp.kerflow.domain.enums.SignalType;
import com.kerflowapp.kerflow.services.search.WebsiteScraperService;
import com.kerflowapp.kerflow.services.search.WebsiteScraperService.WebsiteAudit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Turns the website audit into signals. Reuses the audit already present on the
 * context (search flow) or fetches the site itself (enrich-on-demand flow).
 */
@Component
@RequiredArgsConstructor
public class WebsiteSignalEnricher implements Enricher {

    private static final String ID = "website";

    private static final Set<String> GENERIC_EMAIL_DOMAINS = Set.of(
        "gmail.com", "hotmail.com", "hotmail.fr", "yahoo.com", "yahoo.fr",
        "orange.fr", "wanadoo.fr", "free.fr", "sfr.fr", "laposte.net",
        "outlook.com", "outlook.fr"
    );

    private final WebsiteScraperService websiteScraperService;

    @Override
    public String id() {
        return ID;
    }

    @Override
    public int order() {
        return 20;
    }

    /**
     * On creation these signals already came from the search — re-scraping the site would
     * only duplicate what the frontend just sent, at the cost of an HTTP fetch.
     */
    @Override
    public boolean supports(EnrichmentContext context) {
        return context.getMode() != EnrichmentMode.CREATION;
    }

    @Override
    public void enrich(EnrichmentContext context) {
        String website = context.getWebsite();
        if (website == null || website.isBlank()) {
            context.addSignal(SignalType.NO_WEBSITE, SignalImportance.HIGH, ID, null);
            return;
        }

        WebsiteAudit audit = context.getWebsiteAudit();
        if (audit == null) {
            audit = websiteScraperService.scrape(website);
            context.setWebsiteAudit(audit);
        }

        // An empty audit (fetch failed) has no finalUrl: emitting NO_HTTPS & co would be false facts.
        if (audit.finalUrl() == null) {
            return;
        }

        if (!audit.https()) {
            context.addSignal(SignalType.NO_HTTPS, SignalImportance.MEDIUM, ID, null);
        } else {
            context.addSignal(SignalType.PROFESSIONAL_WEBSITE, SignalImportance.MEDIUM, ID, null);
        }
        int socialCount = countSocialLinks(audit.socialLinks());
        if (socialCount > 0) {
            context.addSignal(SignalType.SOCIAL_MEDIA_PRESENCE, SignalImportance.LOW, ID, Map.of("count", socialCount));
        }
        if (audit.wordpress()) {
            context.addSignal(SignalType.WORDPRESS, SignalImportance.LOW, ID, null);
        }
        if (audit.recruitingHints()) {
            context.addSignal(SignalType.RECRUITING_DETECTED, SignalImportance.HIGH, ID, null);
        }
        if (audit.fleetHints()) {
            context.addSignal(SignalType.FLEET_DETECTED, SignalImportance.HIGH, ID, null);
        }
        addEmailSignal(context, audit);
    }

    private int countSocialLinks(WebsiteScraperService.SocialLinks links) {
        if (links == null) {
            return 0;
        }
        int count = 0;
        for (String link : new String[]{links.facebook(), links.instagram(), links.linkedin(),
            links.twitter(), links.tiktok(), links.youtube()}) {
            if (link != null && !link.isBlank()) {
                count++;
            }
        }
        return count;
    }

    private void addEmailSignal(EnrichmentContext context, WebsiteAudit audit) {
        List<String> emails = audit.emails();
        if (emails == null || emails.isEmpty()) {
            return;
        }

        String siteDomain = extractDomain(audit.finalUrl());
        for (String email : emails) {
            String emailDomain = email.substring(email.indexOf('@') + 1);
            if (emailDomain.equals(siteDomain)) {
                context.addSignal(SignalType.PRO_EMAIL, SignalImportance.MEDIUM, ID, Map.of("email", email));
                return;
            }
        }
        for (String email : emails) {
            String emailDomain = email.substring(email.indexOf('@') + 1);
            if (GENERIC_EMAIL_DOMAINS.contains(emailDomain)) {
                context.addSignal(SignalType.GENERIC_EMAIL, SignalImportance.LOW, ID, Map.of("email", email));
                return;
            }
        }
    }

    private String extractDomain(String url) {
        try {
            String host = URI.create(url).getHost();
            if (host == null) {
                return null;
            }
            host = host.toLowerCase(Locale.ROOT);
            return host.startsWith("www.") ? host.substring(4) : host;
        } catch (Exception e) {
            return null;
        }
    }
}
