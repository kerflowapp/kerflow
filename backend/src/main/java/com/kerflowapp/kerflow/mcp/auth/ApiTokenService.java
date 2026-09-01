package com.kerflowapp.kerflow.mcp.auth;

import com.kerflowapp.kerflow.domain.ApiToken;
import com.kerflowapp.kerflow.domain.OAuthClient;
import com.kerflowapp.kerflow.domain.User;
import com.kerflowapp.kerflow.exceptions.KerflowException;
import com.kerflowapp.kerflow.oauth.Secrets;
import com.kerflowapp.kerflow.repositories.ApiTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.kerflowapp.kerflow.exceptions.KerflowException.ErrorCode.INVALID_TOKEN;

/**
 * Bearer tokens for MCP clients. The plaintext token ("kf_" + 256-bit random) is returned
 * once at creation; only its SHA-256 hash is stored. Personal tokens are created by hand
 * and never expire; OAuth tokens are minted by the authorization code flow and do.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApiTokenService {

    public static final String TOKEN_PREFIX = "kf_";
    private static final int DISPLAY_PREFIX_LENGTH = 12;
    private static final Duration LAST_USED_UPDATE_THROTTLE = Duration.ofHours(1);

    private final ApiTokenRepository apiTokenRepository;

    public record CreatedToken(ApiToken apiToken, String plaintext) {
    }

    public CreatedToken createToken(User user, String name) {
        return persist(ApiToken.builder().name(name).user(user));
    }

    /**
     * Mints an access token for an OAuth client. Same opaque shape as a personal token, so
     * {@link McpApiTokenFilter} needs no special case.
     */
    public CreatedToken createOAuthToken(User user, OAuthClient client, String scope, Duration ttl) {
        return persist(ApiToken.builder()
            .name(client.getClientName())
            .user(user)
            .client(client)
            .scope(scope)
            .expiresAt(Instant.now().plus(ttl)));
    }

    private CreatedToken persist(ApiToken.ApiTokenBuilder builder) {
        String plaintext = TOKEN_PREFIX + Secrets.random();

        ApiToken apiToken = builder
            .tokenHash(Secrets.sha256Hex(plaintext))
            .tokenPrefix(plaintext.substring(0, DISPLAY_PREFIX_LENGTH))
            .build();

        return new CreatedToken(apiTokenRepository.save(apiToken), plaintext);
    }

    public List<ApiToken> getTokens(User user) {
        return apiTokenRepository.findPersonalByUserId(user.getId());
    }

    public void revokeToken(User user, UUID tokenId) {
        ApiToken apiToken = apiTokenRepository.findByIdAndUserId(tokenId, user.getId())
            .orElseThrow(() -> new KerflowException(INVALID_TOKEN));
        apiToken.setRevokedAt(Instant.now());
        apiTokenRepository.save(apiToken);
    }

    /**
     * Revokes every access token a given OAuth client holds for a user (used when the user
     * disconnects the app, and when a refresh token is replayed).
     */
    @Transactional
    public int revokeOAuthTokens(UUID userId, UUID clientId) {
        return apiTokenRepository.revokeByUserAndClient(userId, clientId, Instant.now());
    }

    /**
     * Resolves a plaintext bearer token to its owner. Updates lastUsedAt at most once per hour
     * to avoid a write per MCP tool call.
     */
    @Transactional
    public Optional<User> resolveUser(String plaintext) {
        if (plaintext == null || !plaintext.startsWith(TOKEN_PREFIX)) {
            return Optional.empty();
        }
        Instant now = Instant.now();
        return apiTokenRepository.findActiveByTokenHash(Secrets.sha256Hex(plaintext), now)
            .map(apiToken -> {
                if (apiToken.getLastUsedAt() == null
                    || apiToken.getLastUsedAt().isBefore(now.minus(LAST_USED_UPDATE_THROTTLE))) {
                    apiToken.setLastUsedAt(now);
                    apiTokenRepository.save(apiToken);
                }
                return apiToken.getUser();
            });
    }
}
