package com.kerflowapp.kerflow.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

/**
 * A short-lived, single-use authorization code minted once the user approved a consent
 * request. Only the SHA-256 hash of the code is stored.
 */
@Entity
@Table(name = "oauth_authorization_codes")
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class OAuthAuthorizationCode extends AbstractAuditing {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(unique = true, nullable = false)
    private String codeHash;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "oauth_client_id", nullable = false)
    private OAuthClient client;

    /**
     * Must match the redirect_uri replayed at the token endpoint.
     */
    @Column(nullable = false)
    private String redirectUri;

    @Column(nullable = false)
    private String codeChallenge;

    private String scope;

    private String resource;

    @Column(nullable = false)
    private Instant expiresAt;

    private Instant consumedAt;

    public boolean isUsable(Instant now) {
        return consumedAt == null && expiresAt.isAfter(now);
    }

}
