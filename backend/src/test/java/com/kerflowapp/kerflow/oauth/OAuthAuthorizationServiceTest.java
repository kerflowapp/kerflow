package com.kerflowapp.kerflow.oauth;

import com.kerflowapp.kerflow.domain.OAuthAuthorizationCode;
import com.kerflowapp.kerflow.domain.OAuthAuthorizationRequest;
import com.kerflowapp.kerflow.domain.OAuthClient;
import com.kerflowapp.kerflow.domain.User;
import com.kerflowapp.kerflow.repositories.OAuthAuthorizationCodeRepository;
import com.kerflowapp.kerflow.repositories.OAuthAuthorizationRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OAuthAuthorizationServiceTest {

    private static final String REDIRECT_URI = "https://claude.ai/api/mcp/auth_callback";
    // RFC 7636 appendix B reference pair.
    private static final String VERIFIER = "dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk";
    private static final String CHALLENGE = "E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM";

    private OAuthAuthorizationRequestRepository requestRepository;
    private OAuthAuthorizationCodeRepository codeRepository;
    private OAuthAuthorizationService service;

    private OAuthClient client;
    private User user;

    @BeforeEach
    void setUp() {
        requestRepository = mock(OAuthAuthorizationRequestRepository.class);
        codeRepository = mock(OAuthAuthorizationCodeRepository.class);
        service = new OAuthAuthorizationService(requestRepository, codeRepository);

        client = OAuthClient.builder()
            .id(UUID.randomUUID())
            .clientId("client-abc")
            .clientName("Claude")
            .redirectUris(Set.of(REDIRECT_URI))
            .tokenEndpointAuthMethod(OAuthClient.AUTH_METHOD_NONE)
            .build();

        user = new User();
        user.setId(UUID.randomUUID());

        when(requestRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(codeRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private OAuthAuthorizationCode storedCode() {
        return OAuthAuthorizationCode.builder()
            .id(UUID.randomUUID())
            .codeHash(Secrets.sha256Hex("the-code"))
            .user(user)
            .client(client)
            .redirectUri(REDIRECT_URI)
            .codeChallenge(CHALLENGE)
            .expiresAt(Instant.now().plusSeconds(60))
            .build();
    }

    private void givenStoredCode(OAuthAuthorizationCode code) {
        when(codeRepository.findByCodeHash(Secrets.sha256Hex("the-code"))).thenReturn(Optional.of(code));
    }

    @Test
    void park_rejects_a_redirect_uri_the_client_did_not_register() {
        assertThatThrownBy(() -> service.park(
            client, "https://evil.example/callback", "st", CHALLENGE, "S256", "mcp", null))
            .isInstanceOf(OAuthException.class)
            .hasMessageContaining("redirect_uri");

        verify(requestRepository, never()).save(any());
    }

    @Test
    void park_requires_pkce_with_s256() {
        assertThatThrownBy(() -> service.park(client, REDIRECT_URI, "st", null, "S256", "mcp", null))
            .isInstanceOf(OAuthException.class)
            .hasMessageContaining("code_challenge");

        assertThatThrownBy(() -> service.park(client, REDIRECT_URI, "st", CHALLENGE, "plain", "mcp", null))
            .isInstanceOf(OAuthException.class)
            .hasMessageContaining("S256");
    }

    @Test
    void park_defaults_the_scope_to_mcp() {
        OAuthAuthorizationRequest request = service.park(client, REDIRECT_URI, "st", CHALLENGE, "S256", "  ", null);

        assertThat(request.getScope()).isEqualTo(OAuthClientService.SCOPE_MCP);
        assertThat(request.getExpiresAt()).isAfter(Instant.now());
    }

    @Test
    void approve_consumes_the_request_and_returns_a_callback_carrying_code_and_state() {
        OAuthAuthorizationRequest request = OAuthAuthorizationRequest.builder()
            .id(UUID.randomUUID())
            .client(client)
            .redirectUri(REDIRECT_URI)
            .state("state 123")
            .codeChallenge(CHALLENGE)
            .codeChallengeMethod("S256")
            .scope("mcp")
            .expiresAt(Instant.now().plusSeconds(600))
            .build();
        when(requestRepository.findByIdWithClient(request.getId())).thenReturn(Optional.of(request));

        OAuthAuthorizationService.IssuedCode issued = service.approve(request.getId(), user);

        assertThat(request.getConsumedAt()).isNotNull();
        assertThat(issued.redirectUri())
            .startsWith(REDIRECT_URI + "?code=")
            .contains("state=state+123");

        ArgumentCaptor<OAuthAuthorizationCode> saved = ArgumentCaptor.forClass(OAuthAuthorizationCode.class);
        verify(codeRepository).save(saved.capture());
        // Only the hash is persisted; the plaintext code lives in the redirect and nowhere else.
        assertThat(saved.getValue().getCodeHash()).isEqualTo(Secrets.sha256Hex(issued.code()));
        assertThat(saved.getValue().getCodeHash()).isNotEqualTo(issued.code());
    }

    @Test
    void an_already_consumed_request_cannot_be_approved_twice() {
        OAuthAuthorizationRequest request = OAuthAuthorizationRequest.builder()
            .id(UUID.randomUUID())
            .client(client)
            .redirectUri(REDIRECT_URI)
            .codeChallenge(CHALLENGE)
            .codeChallengeMethod("S256")
            .expiresAt(Instant.now().plusSeconds(600))
            .consumedAt(Instant.now())
            .build();
        when(requestRepository.findByIdWithClient(request.getId())).thenReturn(Optional.of(request));

        assertThatThrownBy(() -> service.approve(request.getId(), user))
            .isInstanceOf(OAuthException.class)
            .hasMessageContaining("already used");
    }

    @Test
    void consumeCode_accepts_a_matching_verifier_and_marks_the_code_used() {
        OAuthAuthorizationCode code = storedCode();
        givenStoredCode(code);

        OAuthAuthorizationCode consumed = service.consumeCode("the-code", client, REDIRECT_URI, VERIFIER);

        assertThat(consumed.getUser()).isEqualTo(user);
        assertThat(consumed.getConsumedAt()).isNotNull();
    }

    @Test
    void consumeCode_rejects_a_wrong_verifier() {
        givenStoredCode(storedCode());

        assertThatThrownBy(() -> service.consumeCode("the-code", client, REDIRECT_URI, "wrong-verifier"))
            .isInstanceOf(OAuthException.class)
            .hasMessageContaining("code_verifier");
    }

    @Test
    void consumeCode_rejects_a_replayed_code() {
        OAuthAuthorizationCode code = storedCode();
        code.setConsumedAt(Instant.now());
        givenStoredCode(code);

        assertThatThrownBy(() -> service.consumeCode("the-code", client, REDIRECT_URI, VERIFIER))
            .isInstanceOf(OAuthException.class)
            .hasMessageContaining("already used");
    }

    @Test
    void consumeCode_rejects_an_expired_code() {
        OAuthAuthorizationCode code = storedCode();
        code.setExpiresAt(Instant.now().minusSeconds(1));
        givenStoredCode(code);

        assertThatThrownBy(() -> service.consumeCode("the-code", client, REDIRECT_URI, VERIFIER))
            .isInstanceOf(OAuthException.class)
            .hasMessageContaining("expired");
    }

    @Test
    void consumeCode_rejects_a_code_issued_to_another_client() {
        givenStoredCode(storedCode());

        OAuthClient otherClient = OAuthClient.builder()
            .id(UUID.randomUUID())
            .clientId("client-xyz")
            .tokenEndpointAuthMethod(OAuthClient.AUTH_METHOD_NONE)
            .build();

        assertThatThrownBy(() -> service.consumeCode("the-code", otherClient, REDIRECT_URI, VERIFIER))
            .isInstanceOf(OAuthException.class)
            .hasMessageContaining("not issued to this client");
    }

    @Test
    void consumeCode_rejects_a_redirect_uri_that_changed_between_the_two_requests() {
        givenStoredCode(storedCode());

        assertThatThrownBy(() -> service.consumeCode("the-code", client, "https://claude.ai/other", VERIFIER))
            .isInstanceOf(OAuthException.class)
            .hasMessageContaining("redirect_uri");
    }

    @Test
    void consumeCode_rejects_an_unknown_code() {
        when(codeRepository.findByCodeHash(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.consumeCode("nope", client, REDIRECT_URI, VERIFIER))
            .isInstanceOf(OAuthException.class)
            .hasMessageContaining("invalid authorization code");
    }

    @Test
    void deny_returns_an_access_denied_callback() {
        OAuthAuthorizationRequest request = OAuthAuthorizationRequest.builder()
            .id(UUID.randomUUID())
            .client(client)
            .redirectUri(REDIRECT_URI)
            .state("xyz")
            .codeChallenge(CHALLENGE)
            .codeChallengeMethod("S256")
            .expiresAt(Instant.now().plusSeconds(600))
            .build();
        when(requestRepository.findByIdWithClient(request.getId())).thenReturn(Optional.of(request));

        assertThat(service.deny(request.getId()))
            .isEqualTo(REDIRECT_URI + "?error=access_denied&state=xyz");
        assertThat(request.getConsumedAt()).isNotNull();
        verify(codeRepository, never()).save(any());
    }
}
