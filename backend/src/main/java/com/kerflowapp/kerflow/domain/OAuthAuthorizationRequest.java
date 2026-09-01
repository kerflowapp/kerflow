package com.kerflowapp.kerflow.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

/**
 * A pending /oauth2/authorize request, parked while the user is sent to the SPA consent
 * screen. The id is the opaque request_id handed to the frontend; it carries no privilege
 * on its own (approving it requires the user's Cognito JWT).
 */
@Entity
@Table(name = "oauth_authorization_requests")
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class OAuthAuthorizationRequest extends AbstractAuditing {

    @Id
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "oauth_client_id", nullable = false)
    private OAuthClient client;

    @Column(nullable = false)
    private String redirectUri;

    /**
     * Opaque to us: relayed verbatim to the client callback, never interpreted.
     */
    @Column(length = 2048)
    private String state;

    @Column(nullable = false)
    private String codeChallenge;

    @Column(nullable = false)
    private String codeChallengeMethod;

    private String scope;

    /**
     * RFC 8707 resource indicator, as sent by the client.
     */
    private String resource;

    @Column(nullable = false)
    private Instant expiresAt;

    private Instant consumedAt;

    public boolean isUsable(Instant now) {
        return consumedAt == null && expiresAt.isAfter(now);
    }

}
