package com.kerflowapp.kerflow.oauth;

import com.kerflowapp.kerflow.domain.OAuthAuthorizationCode;
import com.kerflowapp.kerflow.domain.OAuthClient;
import com.kerflowapp.kerflow.domain.OAuthRefreshToken;
import com.kerflowapp.kerflow.domain.User;
import com.kerflowapp.kerflow.mcp.auth.ApiTokenService;
import com.kerflowapp.kerflow.oauth.domain.TokenResponse;
import com.kerflowapp.kerflow.repositories.OAuthRefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * The token endpoint half of the flow. Access tokens are ordinary opaque "kf_" api_tokens
 * with an expiry, so /mcp validates them through the existing filter with no special case.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OAuthTokenService {

    public static final String GRANT_AUTHORIZATION_CODE = "authorization_code";
    public static final String GRANT_REFRESH_TOKEN = "refresh_token";

    private static final Duration ACCESS_TOKEN_TTL = Duration.ofHours(1);
    private static final Duration REFRESH_TOKEN_TTL = Duration.ofDays(30);

    private final ApiTokenService apiTokenService;
    private final OAuthAuthorizationService authorizationService;
    private final OAuthRefreshTokenRepository refreshTokenRepository;

    @Transactional
    public TokenResponse exchangeAuthorizationCode(OAuthClient client,
                                                   String code,
                                                   String redirectUri,
                                                   String codeVerifier) {

        if (code == null || code.isBlank()) {
            throw OAuthException.invalidRequest("code is required");
        }
        if (redirectUri == null || redirectUri.isBlank()) {
            throw OAuthException.invalidRequest("redirect_uri is required");
        }
        if (codeVerifier == null || codeVerifier.isBlank()) {
            throw OAuthException.invalidRequest("code_verifier is required (PKCE)");
        }

        OAuthAuthorizationCode authorizationCode = authorizationService.consumeCode(code, client, redirectUri, codeVerifier);

        return issue(authorizationCode.getUser(), client, authorizationCode.getScope(), authorizationCode.getResource());
    }

    /**
     * Refresh with rotation: the presented token is consumed and replaced. Replaying a
     * consumed token means it leaked, so every access token this client holds for the user
     * is revoked (OAuth 2.1, section 4.3.1).
     */
    @Transactional
    public TokenResponse refresh(OAuthClient client, String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw OAuthException.invalidRequest("refresh_token is required");
        }

        OAuthRefreshToken stored = refreshTokenRepository.findByTokenHash(Secrets.sha256Hex(refreshToken))
            .orElseThrow(() -> OAuthException.invalidGrant("invalid refresh_token"));

        if (!stored.getClient().getId().equals(client.getId())) {
            throw OAuthException.invalidGrant("refresh_token was not issued to this client");
        }
        if (stored.getConsumedAt() != null) {
            LOGGER.warn("Replayed refresh token for user {} / client '{}': revoking the whole grant",
                stored.getUser().getId(), client.getClientName());
            revokeGrant(stored.getUser().getId(), client.getId());
            throw OAuthException.invalidGrant("refresh_token already used");
        }
        if (!stored.isUsable(Instant.now())) {
            throw OAuthException.invalidGrant("refresh_token expired or revoked");
        }

        stored.setConsumedAt(Instant.now());
        refreshTokenRepository.save(stored);

        return issue(stored.getUser(), client, stored.getScope(), stored.getResource());
    }

    private TokenResponse issue(User user, OAuthClient client, String scope, String resource) {
        ApiTokenService.CreatedToken accessToken =
            apiTokenService.createOAuthToken(user, client, scope, ACCESS_TOKEN_TTL);

        String refreshToken = Secrets.random();
        refreshTokenRepository.save(OAuthRefreshToken.builder()
            .tokenHash(Secrets.sha256Hex(refreshToken))
            .user(user)
            .client(client)
            .scope(scope)
            .resource(resource)
            .expiresAt(Instant.now().plus(REFRESH_TOKEN_TTL))
            .build());

        return TokenResponse.builder()
            .accessToken(accessToken.plaintext())
            .tokenType("Bearer")
            .expiresIn(ACCESS_TOKEN_TTL.toSeconds())
            .refreshToken(refreshToken)
            .scope(scope)
            .build();
    }

    public record OAuthConnection(UUID clientId, String clientName, LocalDateTime lastActivity) {
    }

    /**
     * The apps currently holding a grant for this user: one entry per client with a usable
     * refresh token. Refresh tokens rotate on every use, so the newest one dates the last activity.
     */
    @Transactional(readOnly = true)
    public List<OAuthConnection> getConnections(User user) {
        Map<UUID, OAuthConnection> connections = new LinkedHashMap<>();
        refreshTokenRepository.findUsableByUser(user.getId(), Instant.now())
            .forEach(token -> connections.putIfAbsent(token.getClient().getId(), new OAuthConnection(
                token.getClient().getId(),
                token.getClient().getClientName(),
                token.getCreationDate())));
        return List.copyOf(connections.values());
    }

    /**
     * Revokes every access and refresh token a client holds for a user. Used when the user
     * disconnects an app, and when a refresh token is replayed. Scoped by user, so a client id
     * alone never reaches another user's grant.
     */
    @Transactional
    public void revokeGrant(UUID userId, UUID clientId) {
        apiTokenService.revokeOAuthTokens(userId, clientId);
        Instant now = Instant.now();
        refreshTokenRepository.findActiveByUserAndClient(userId, clientId)
            .forEach(token -> {
                token.setRevokedAt(now);
                refreshTokenRepository.save(token);
            });
    }
}
