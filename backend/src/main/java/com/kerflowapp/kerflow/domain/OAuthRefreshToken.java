package com.kerflowapp.kerflow.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

/**
 * A refresh token issued alongside an OAuth access token. Kept in its own table rather
 * than in api_tokens on purpose: a refresh token must never authenticate an /mcp call.
 * Rotated on every use (the clients we serve are public).
 */
@Entity
@Table(name = "oauth_refresh_tokens")
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class OAuthRefreshToken extends AbstractAuditing {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(unique = true, nullable = false)
    private String tokenHash;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "oauth_client_id", nullable = false)
    private OAuthClient client;

    private String scope;

    private String resource;

    @Column(nullable = false)
    private Instant expiresAt;

    /**
     * Set when rotated: the token has been exchanged for a new one.
     */
    private Instant consumedAt;

    private Instant revokedAt;

    public boolean isUsable(Instant now) {
        return consumedAt == null && revokedAt == null && expiresAt.isAfter(now);
    }

}
