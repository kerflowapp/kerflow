package com.kerflowapp.kerflow.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * An OAuth client allowed to request access to the /mcp endpoint on behalf of a user.
 * Created through dynamic client registration (RFC 7591) by MCP clients such as Claude.ai.
 * Public clients have no secret ({@code tokenEndpointAuthMethod = "none"}).
 */
@Entity
@Table(name = "oauth_clients")
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class OAuthClient extends AbstractAuditing {

    public static final String AUTH_METHOD_NONE = "none";

    @Id
    @UuidGenerator
    private UUID id;

    @Column(unique = true, nullable = false)
    private String clientId;

    /**
     * SHA-256 of the client secret. Null for public clients.
     */
    private String clientSecretHash;

    private String clientName;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "oauth_client_redirect_uris", joinColumns = @JoinColumn(name = "oauth_client_id"))
    @Column(name = "redirect_uri", nullable = false)
    @Builder.Default
    private Set<String> redirectUris = new HashSet<>();

    private String scope;

    @Column(nullable = false)
    private String tokenEndpointAuthMethod;

    public boolean isPublicClient() {
        return AUTH_METHOD_NONE.equals(tokenEndpointAuthMethod);
    }

    /**
     * Exact match only: prefix or wildcard matching would open a redirect attack.
     */
    public boolean allowsRedirectUri(String redirectUri) {
        return redirectUri != null && redirectUris.contains(redirectUri);
    }

}
