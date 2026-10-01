package com.kerflowapp.kerflow.oauth;

import com.kerflowapp.kerflow.domain.OAuthClient;
import com.kerflowapp.kerflow.domain.OAuthRefreshToken;
import com.kerflowapp.kerflow.domain.User;
import com.kerflowapp.kerflow.mcp.auth.ApiTokenService;
import com.kerflowapp.kerflow.repositories.OAuthRefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class OAuthTokenServiceTest {

    private ApiTokenService apiTokenService;
    private OAuthRefreshTokenRepository refreshTokenRepository;
    private OAuthTokenService service;

    private User user;
    private OAuthClient claude;
    private OAuthClient chatgpt;

    @BeforeEach
    void setUp() {
        apiTokenService = mock(ApiTokenService.class);
        refreshTokenRepository = mock(OAuthRefreshTokenRepository.class);
        service = new OAuthTokenService(apiTokenService, mock(OAuthAuthorizationService.class), refreshTokenRepository);

        user = new User();
        user.setId(UUID.randomUUID());
        claude = client("Claude");
        chatgpt = client("ChatGPT");
    }

    private static OAuthClient client(String name) {
        return OAuthClient.builder()
            .id(UUID.randomUUID())
            .clientId(name.toLowerCase())
            .clientName(name)
            .tokenEndpointAuthMethod(OAuthClient.AUTH_METHOD_NONE)
            .build();
    }

    private OAuthRefreshToken refreshToken(OAuthClient client, LocalDateTime createdAt) {
        OAuthRefreshToken token = OAuthRefreshToken.builder()
            .id(UUID.randomUUID())
            .tokenHash(Secrets.sha256Hex(UUID.randomUUID().toString()))
            .user(user)
            .client(client)
            .expiresAt(Instant.now().plusSeconds(3600))
            .build();
        token.setCreationDate(createdAt);
        return token;
    }

    @Test
    void getConnections_lists_each_client_once_dated_by_its_newest_refresh_token() {
        LocalDateTime now = LocalDateTime.now();
        when(refreshTokenRepository.findUsableByUser(eq(user.getId()), any())).thenReturn(List.of(
            refreshToken(claude, now),
            refreshToken(chatgpt, now.minusHours(2)),
            refreshToken(claude, now.minusDays(1))));

        List<OAuthTokenService.OAuthConnection> connections = service.getConnections(user);

        assertThat(connections).containsExactly(
            new OAuthTokenService.OAuthConnection(claude.getId(), "Claude", now),
            new OAuthTokenService.OAuthConnection(chatgpt.getId(), "ChatGPT", now.minusHours(2)));
    }

    @Test
    void revokeGrant_revokes_access_and_refresh_tokens_of_the_user_for_that_client() {
        OAuthRefreshToken active = refreshToken(claude, LocalDateTime.now());
        when(refreshTokenRepository.findActiveByUserAndClient(user.getId(), claude.getId())).thenReturn(List.of(active));

        service.revokeGrant(user.getId(), claude.getId());

        verify(apiTokenService).revokeOAuthTokens(user.getId(), claude.getId());
        assertThat(active.getRevokedAt()).isNotNull();
        verify(refreshTokenRepository).save(active);
    }

    @Test
    void refresh_revokes_the_whole_grant_when_a_consumed_token_is_replayed() {
        OAuthRefreshToken consumed = refreshToken(claude, LocalDateTime.now());
        consumed.setConsumedAt(Instant.now().minusSeconds(60));
        when(refreshTokenRepository.findByTokenHash(Secrets.sha256Hex("replayed"))).thenReturn(Optional.of(consumed));
        when(refreshTokenRepository.findActiveByUserAndClient(user.getId(), claude.getId())).thenReturn(List.of());

        assertThatThrownBy(() -> service.refresh(claude, "replayed"))
            .isInstanceOf(OAuthException.class)
            .hasMessageContaining("already used");

        verify(apiTokenService).revokeOAuthTokens(user.getId(), claude.getId());
    }
}
