package com.kerflowapp.kerflow.oauth;

import com.kerflowapp.kerflow.domain.OAuthAuthorizationRequest;
import com.kerflowapp.kerflow.domain.OAuthClient;
import com.kerflowapp.kerflow.oauth.domain.TokenResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

/**
 * The two public endpoints of the authorization code flow.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class OAuthAuthorizationController {

    private static final String BASIC_PREFIX = "Basic ";

    private final OAuthClientService clientService;
    private final OAuthAuthorizationService authorizationService;
    private final OAuthTokenService tokenService;
    private final OAuthUrls oauthUrls;

    /**
     * Parks the request and bounces the browser to the SPA consent screen. Note that errors
     * here are rendered as JSON rather than redirected: at this point either client_id or
     * redirect_uri failed validation, and redirecting to an unvalidated URI is the open
     * redirect this check exists to prevent.
     */
    @GetMapping(OAuthUrls.AUTHORIZE_PATH)
    public ResponseEntity<Void> authorize(
        @RequestParam(value = "response_type", required = false) String responseType,
        @RequestParam(value = "client_id", required = false) String clientId,
        @RequestParam(value = "redirect_uri", required = false) String redirectUri,
        @RequestParam(value = "state", required = false) String state,
        @RequestParam(value = "code_challenge", required = false) String codeChallenge,
        @RequestParam(value = "code_challenge_method", required = false) String codeChallengeMethod,
        @RequestParam(value = "scope", required = false) String scope,
        @RequestParam(value = "resource", required = false) String resource) {

        if (!"code".equals(responseType)) {
            throw OAuthException.invalidRequest("response_type must be code");
        }

        OAuthClient client = clientService.requireClient(clientId);
        OAuthAuthorizationRequest request = authorizationService.park(
            client, redirectUri, state, codeChallenge, codeChallengeMethod, scope, resource);

        return ResponseEntity.status(HttpStatus.FOUND)
            .location(URI.create(oauthUrls.consentPage(request.getId().toString())))
            .build();
    }

    @PostMapping(value = OAuthUrls.TOKEN_PATH, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<TokenResponse> token(
        @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
        @RequestParam Map<String, String> params) {

        ClientCredentials credentials = resolveCredentials(authorization, params);
        OAuthClient client = clientService.requireClient(credentials.clientId());
        clientService.authenticate(client, credentials.clientSecret());

        String grantType = params.get("grant_type");
        TokenResponse response = switch (grantType == null ? "" : grantType) {
            case OAuthTokenService.GRANT_AUTHORIZATION_CODE -> tokenService.exchangeAuthorizationCode(
                client,
                params.get("code"),
                params.get("redirect_uri"),
                params.get("code_verifier"));
            case OAuthTokenService.GRANT_REFRESH_TOKEN -> tokenService.refresh(client, params.get("refresh_token"));
            default -> throw OAuthException.unsupportedGrantType("unsupported grant_type: " + grantType);
        };

        return ResponseEntity.ok()
            .header("Cache-Control", "no-store")
            .header("Pragma", "no-cache")
            .body(response);
    }

    private record ClientCredentials(String clientId, String clientSecret) {
    }

    /**
     * Supports client_secret_basic, client_secret_post, and public clients (client_id only).
     */
    private ClientCredentials resolveCredentials(String authorization, Map<String, String> params) {
        if (authorization != null && authorization.startsWith(BASIC_PREFIX)) {
            String decoded = new String(
                Base64.getDecoder().decode(authorization.substring(BASIC_PREFIX.length()).trim()),
                StandardCharsets.UTF_8);
            int separator = decoded.indexOf(':');
            if (separator < 0) {
                throw OAuthException.invalidClient("malformed Basic credentials");
            }
            return new ClientCredentials(decoded.substring(0, separator), decoded.substring(separator + 1));
        }
        return new ClientCredentials(params.get("client_id"), params.get("client_secret"));
    }
}
