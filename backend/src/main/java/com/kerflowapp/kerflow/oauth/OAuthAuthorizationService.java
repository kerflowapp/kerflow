package com.kerflowapp.kerflow.oauth;

import com.kerflowapp.kerflow.domain.OAuthAuthorizationCode;
import com.kerflowapp.kerflow.domain.OAuthAuthorizationRequest;
import com.kerflowapp.kerflow.domain.OAuthClient;
import com.kerflowapp.kerflow.domain.User;
import com.kerflowapp.kerflow.repositories.OAuthAuthorizationCodeRepository;
import com.kerflowapp.kerflow.repositories.OAuthAuthorizationRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * The authorization endpoint half of the flow: parks an incoming /oauth2/authorize request
 * until the user decides, then turns an approval into a single-use authorization code.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OAuthAuthorizationService {

    public static final String CODE_CHALLENGE_METHOD_S256 = "S256";
    private static final Duration REQUEST_TTL = Duration.ofMinutes(10);
    private static final Duration CODE_TTL = Duration.ofMinutes(1);

    private final OAuthAuthorizationRequestRepository authorizationRequestRepository;
    private final OAuthAuthorizationCodeRepository authorizationCodeRepository;

    public record IssuedCode(String code, String redirectUri) {
    }

    /**
     * Validates an /oauth2/authorize request and parks it. The caller redirects the browser
     * to the consent screen with the returned id.
     *
     * <p>Anything wrong with client_id or redirect_uri must be surfaced as a plain error and
     * never redirected: an unvalidated redirect_uri is an open redirect.
     */
    @Transactional
    public OAuthAuthorizationRequest park(OAuthClient client,
                                          String redirectUri,
                                          String state,
                                          String codeChallenge,
                                          String codeChallengeMethod,
                                          String scope,
                                          String resource) {

        if (!client.allowsRedirectUri(redirectUri)) {
            throw OAuthException.invalidRequest("redirect_uri does not match a registered value");
        }
        if (codeChallenge == null || codeChallenge.isBlank()) {
            throw OAuthException.invalidRequest("code_challenge is required (PKCE)");
        }
        if (!CODE_CHALLENGE_METHOD_S256.equals(codeChallengeMethod)) {
            throw OAuthException.invalidRequest("code_challenge_method must be S256");
        }

        return authorizationRequestRepository.save(OAuthAuthorizationRequest.builder()
            .client(client)
            .redirectUri(redirectUri)
            .state(state)
            .codeChallenge(codeChallenge)
            .codeChallengeMethod(codeChallengeMethod)
            .scope(scope == null || scope.isBlank() ? OAuthClientService.SCOPE_MCP : scope)
            .resource(resource)
            .expiresAt(Instant.now().plus(REQUEST_TTL))
            .build());
    }

    public OAuthAuthorizationRequest requireUsableRequest(UUID requestId) {
        OAuthAuthorizationRequest request = authorizationRequestRepository.findByIdWithClient(requestId)
            .orElseThrow(() -> OAuthException.invalidRequest("unknown authorization request"));
        if (!request.isUsable(Instant.now())) {
            throw OAuthException.invalidRequest("authorization request expired or already used");
        }
        return request;
    }

    /**
     * Consumes the parked request and mints the authorization code. Returns the callback URL
     * the SPA should navigate to.
     */
    @Transactional
    public IssuedCode approve(UUID requestId, User user) {
        OAuthAuthorizationRequest request = requireUsableRequest(requestId);
        request.setConsumedAt(Instant.now());
        authorizationRequestRepository.save(request);

        String code = Secrets.random();
        authorizationCodeRepository.save(OAuthAuthorizationCode.builder()
            .codeHash(Secrets.sha256Hex(code))
            .user(user)
            .client(request.getClient())
            .redirectUri(request.getRedirectUri())
            .codeChallenge(request.getCodeChallenge())
            .scope(request.getScope())
            .resource(request.getResource())
            .expiresAt(Instant.now().plus(CODE_TTL))
            .build());

        LOGGER.info("User {} approved OAuth client '{}'", user.getId(), request.getClient().getClientName());

        return new IssuedCode(code, buildRedirect(request.getRedirectUri(), "code", code, request.getState()));
    }

    @Transactional
    public String deny(UUID requestId) {
        OAuthAuthorizationRequest request = requireUsableRequest(requestId);
        request.setConsumedAt(Instant.now());
        authorizationRequestRepository.save(request);

        return buildRedirect(request.getRedirectUri(), "error", "access_denied", request.getState());
    }

    /**
     * Redeems a code at the token endpoint. Verifies it is unused, unexpired, bound to this
     * client and redirect_uri, and that the PKCE verifier matches. Marks it consumed in the
     * same transaction so a replay cannot win a race.
     */
    @Transactional
    public OAuthAuthorizationCode consumeCode(String code, OAuthClient client, String redirectUri, String codeVerifier) {
        OAuthAuthorizationCode authorizationCode = authorizationCodeRepository.findByCodeHash(Secrets.sha256Hex(code))
            .orElseThrow(() -> OAuthException.invalidGrant("invalid authorization code"));

        if (!authorizationCode.isUsable(Instant.now())) {
            throw OAuthException.invalidGrant("authorization code expired or already used");
        }
        if (!authorizationCode.getClient().getId().equals(client.getId())) {
            throw OAuthException.invalidGrant("authorization code was not issued to this client");
        }
        if (!authorizationCode.getRedirectUri().equals(redirectUri)) {
            throw OAuthException.invalidGrant("redirect_uri does not match the authorization request");
        }
        if (!Pkce.verify(codeVerifier, authorizationCode.getCodeChallenge())) {
            throw OAuthException.invalidGrant("invalid code_verifier");
        }

        authorizationCode.setConsumedAt(Instant.now());
        return authorizationCodeRepository.save(authorizationCode);
    }

    private String buildRedirect(String redirectUri, String paramName, String paramValue, String state) {
        StringBuilder url = new StringBuilder(redirectUri)
            .append(redirectUri.contains("?") ? '&' : '?')
            .append(paramName).append('=').append(encode(paramValue));
        if (state != null && !state.isBlank()) {
            url.append("&state=").append(encode(state));
        }
        return url.toString();
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
