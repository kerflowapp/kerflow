package com.kerflowapp.kerflow.oauth;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * OAuth discovery documents. These are what turn the 401 on /mcp into a working connector:
 * the client walks resource metadata (RFC 9728) to find the authorization server, then
 * authorization server metadata (RFC 8414) to find the endpoints.
 *
 * <p>Both documents are served on the bare path and on the /mcp-suffixed path, because
 * clients build the URL by inserting the well-known segment before the resource path.
 */
@RestController
@RequiredArgsConstructor
public class OAuthMetadataController {

    private final OAuthUrls oauthUrls;

    @GetMapping({
        "/.well-known/oauth-protected-resource",
        "/.well-known/oauth-protected-resource/mcp"
    })
    public ResponseEntity<Map<String, Object>> protectedResourceMetadata() {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("resource", oauthUrls.resource());
        metadata.put("authorization_servers", List.of(oauthUrls.issuer()));
        metadata.put("scopes_supported", List.of(OAuthClientService.SCOPE_MCP));
        metadata.put("bearer_methods_supported", List.of("header"));
        return ResponseEntity.ok(metadata);
    }

    @GetMapping({
        "/.well-known/oauth-authorization-server",
        "/.well-known/oauth-authorization-server/mcp"
    })
    public ResponseEntity<Map<String, Object>> authorizationServerMetadata() {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("issuer", oauthUrls.issuer());
        metadata.put("authorization_endpoint", oauthUrls.authorizationEndpoint());
        metadata.put("token_endpoint", oauthUrls.tokenEndpoint());
        metadata.put("registration_endpoint", oauthUrls.registrationEndpoint());
        metadata.put("scopes_supported", List.of(OAuthClientService.SCOPE_MCP));
        metadata.put("response_types_supported", List.of("code"));
        metadata.put("grant_types_supported", List.of(
            OAuthTokenService.GRANT_AUTHORIZATION_CODE,
            OAuthTokenService.GRANT_REFRESH_TOKEN));
        metadata.put("code_challenge_methods_supported", List.of(OAuthAuthorizationService.CODE_CHALLENGE_METHOD_S256));
        metadata.put("token_endpoint_auth_methods_supported",
            List.of("client_secret_post", "client_secret_basic", "none"));
        return ResponseEntity.ok(metadata);
    }
}
