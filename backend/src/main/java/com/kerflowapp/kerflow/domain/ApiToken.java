package com.kerflowapp.kerflow.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

/**
 * Bearer token authenticating MCP clients (Claude Code, Claude Desktop, Claude.ai...)
 * on the /mcp endpoint. Only the SHA-256 hash of the token is stored.
 *
 * <p>Two flavours share this table: personal tokens created by hand from the settings
 * page ({@code client == null}, no expiry), and access tokens minted by the OAuth
 * authorization code flow ({@code client != null}, short lived). Both are opaque
 * "kf_" strings, so the /mcp filter treats them identically.
 */
@Entity
@Table(name = "api_tokens")
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ApiToken extends AbstractAuditing {

    @Id
    @UuidGenerator
    private UUID id;

    private String name;

    @Column(unique = true, nullable = false)
    private String tokenHash;

    private String tokenPrefix;

    private Instant lastUsedAt;

    private Instant revokedAt;

    /**
     * Null for personal tokens, which never expire.
     */
    private Instant expiresAt;

    /**
     * Set when the token was issued through the OAuth flow; null for personal tokens.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "oauth_client_id")
    private OAuthClient client;

    private String scope;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public boolean isOAuthToken() {
        return client != null;
    }

}
