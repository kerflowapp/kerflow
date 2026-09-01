package com.kerflowapp.kerflow.oauth;

import com.kerflowapp.kerflow.BaseConfiguration;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Absolute URLs of the authorization server and the protected resource. Everything is
 * derived from commons.backend-url / commons.web-url so the metadata documents, the
 * 401 challenge and the consent redirect can never drift apart.
 */
@Component
@RequiredArgsConstructor
public class OAuthUrls {

    public static final String MCP_PATH = "/mcp";
    public static final String AUTHORIZE_PATH = "/oauth2/authorize";
    public static final String TOKEN_PATH = "/oauth2/token";
    public static final String REGISTRATION_PATH = "/oauth2/register";
    public static final String PROTECTED_RESOURCE_METADATA_PATH = "/.well-known/oauth-protected-resource";
    public static final String CONSENT_PATH = "/oauth/authorize";

    private final BaseConfiguration.CommonsProperties commonsProperties;

    /**
     * The authorization server issuer identifier, and the base of every OAuth endpoint.
     */
    public String issuer() {
        return trimTrailingSlash(commonsProperties.backendUrl());
    }

    /**
     * The canonical resource identifier of the MCP server (RFC 8707 section 2).
     */
    public String resource() {
        return issuer() + MCP_PATH;
    }

    public String authorizationEndpoint() {
        return issuer() + AUTHORIZE_PATH;
    }

    public String tokenEndpoint() {
        return issuer() + TOKEN_PATH;
    }

    public String registrationEndpoint() {
        return issuer() + REGISTRATION_PATH;
    }

    public String protectedResourceMetadata() {
        return issuer() + PROTECTED_RESOURCE_METADATA_PATH;
    }

    /**
     * The SPA screen where the user approves or denies a pending authorization request.
     */
    public String consentPage(String requestId) {
        return trimTrailingSlash(commonsProperties.webUrl()) + CONSENT_PATH + "?request_id=" + requestId;
    }

    private String trimTrailingSlash(String url) {
        return url != null && url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

}
