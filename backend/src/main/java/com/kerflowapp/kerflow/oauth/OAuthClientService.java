package com.kerflowapp.kerflow.oauth;

import com.kerflowapp.kerflow.domain.OAuthClient;
import com.kerflowapp.kerflow.oauth.domain.ClientRegistrationRequest;
import com.kerflowapp.kerflow.oauth.domain.ClientRegistrationResponse;
import com.kerflowapp.kerflow.repositories.OAuthClientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Dynamic client registration (RFC 7591). Registration is open: an MCP client such as
 * Claude.ai registers itself before it has any user context. A registered client is inert
 * until a user approves a consent request for it, so the endpoint grants nothing on its own.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OAuthClientService {

    public static final String SCOPE_MCP = "mcp";
    private static final int MAX_REDIRECT_URIS = 10;
    private static final int CLIENT_ID_BYTES = 16;

    private final OAuthClientRepository oauthClientRepository;

    @Transactional
    public ClientRegistrationResponse register(ClientRegistrationRequest request) {
        Set<String> redirectUris = validateRedirectUris(request.redirectUris());
        validateGrantTypes(request.grantTypes());
        validateResponseTypes(request.responseTypes());

        String authMethod = request.tokenEndpointAuthMethod() == null
            ? "client_secret_post"
            : request.tokenEndpointAuthMethod();
        if (!List.of("client_secret_post", "client_secret_basic", OAuthClient.AUTH_METHOD_NONE).contains(authMethod)) {
            throw OAuthException.invalidClientMetadata("unsupported token_endpoint_auth_method: " + authMethod);
        }

        boolean isPublic = OAuthClient.AUTH_METHOD_NONE.equals(authMethod);
        String clientSecret = isPublic ? null : Secrets.random();

        OAuthClient client = oauthClientRepository.save(OAuthClient.builder()
            .clientId(Secrets.random(CLIENT_ID_BYTES))
            .clientSecretHash(isPublic ? null : Secrets.sha256Hex(clientSecret))
            .clientName(request.clientName() == null ? "MCP client" : request.clientName())
            .redirectUris(redirectUris)
            .scope(SCOPE_MCP)
            .tokenEndpointAuthMethod(authMethod)
            .build());

        LOGGER.info("Registered OAuth client '{}' ({}) with redirect uris {}",
            client.getClientName(), client.getClientId(), redirectUris);

        return ClientRegistrationResponse.builder()
            .clientId(client.getClientId())
            .clientSecret(clientSecret)
            .clientIdIssuedAt(java.time.Instant.now().getEpochSecond())
            .clientName(client.getClientName())
            .redirectUris(List.copyOf(redirectUris))
            .grantTypes(List.of("authorization_code", "refresh_token"))
            .responseTypes(List.of("code"))
            .tokenEndpointAuthMethod(authMethod)
            .scope(SCOPE_MCP)
            .build();
    }

    public OAuthClient requireClient(String clientId) {
        if (clientId == null || clientId.isBlank()) {
            throw OAuthException.invalidClient("client_id is required");
        }
        return oauthClientRepository.findByClientId(clientId)
            .orElseThrow(() -> OAuthException.invalidClient("unknown client_id"));
    }

    /**
     * Authenticates a client at the token endpoint. Public clients are identified by
     * client_id alone (they cannot keep a secret); confidential clients must present theirs.
     */
    public void authenticate(OAuthClient client, String providedSecret) {
        if (client.isPublicClient()) {
            return;
        }
        if (providedSecret == null) {
            throw OAuthException.invalidClient("client_secret is required for this client");
        }
        if (!Secrets.constantTimeEquals(client.getClientSecretHash(), Secrets.sha256Hex(providedSecret))) {
            throw OAuthException.invalidClient("invalid client_secret");
        }
    }

    private Set<String> validateRedirectUris(List<String> redirectUris) {
        if (redirectUris == null || redirectUris.isEmpty()) {
            throw OAuthException.invalidClientMetadata("redirect_uris is required");
        }
        if (redirectUris.size() > MAX_REDIRECT_URIS) {
            throw OAuthException.invalidClientMetadata("too many redirect_uris");
        }
        Set<String> validated = new LinkedHashSet<>();
        for (String redirectUri : redirectUris) {
            validated.add(validateRedirectUri(redirectUri));
        }
        return validated;
    }

    /**
     * OAuth 2.1: redirect URIs must be absolute, https (or loopback http), and fragment-free.
     */
    private String validateRedirectUri(String redirectUri) {
        URI uri;
        try {
            uri = new URI(redirectUri);
        } catch (URISyntaxException e) {
            throw OAuthException.invalidClientMetadata("malformed redirect_uri: " + redirectUri);
        }
        if (!uri.isAbsolute() || uri.getFragment() != null) {
            throw OAuthException.invalidClientMetadata("redirect_uri must be absolute and fragment-free: " + redirectUri);
        }
        boolean isHttps = "https".equalsIgnoreCase(uri.getScheme());
        boolean isLoopback = "http".equalsIgnoreCase(uri.getScheme()) && isLoopbackHost(uri.getHost());
        if (!isHttps && !isLoopback) {
            throw OAuthException.invalidClientMetadata("redirect_uri must use https (or http on loopback): " + redirectUri);
        }
        return redirectUri;
    }

    private boolean isLoopbackHost(String host) {
        return "localhost".equalsIgnoreCase(host) || "127.0.0.1".equals(host) || "[::1]".equals(host) || "::1".equals(host);
    }

    private void validateGrantTypes(List<String> grantTypes) {
        if (grantTypes == null || grantTypes.isEmpty()) {
            return;
        }
        List<String> supported = List.of("authorization_code", "refresh_token");
        grantTypes.stream()
            .filter(grantType -> !supported.contains(grantType))
            .findFirst()
            .ifPresent(grantType -> {
                throw OAuthException.invalidClientMetadata("unsupported grant_type: " + grantType);
            });
    }

    private void validateResponseTypes(List<String> responseTypes) {
        if (responseTypes == null || responseTypes.isEmpty()) {
            return;
        }
        responseTypes.stream()
            .filter(responseType -> !"code".equals(responseType))
            .findFirst()
            .ifPresent(responseType -> {
                throw OAuthException.invalidClientMetadata("unsupported response_type: " + responseType);
            });
    }
}
