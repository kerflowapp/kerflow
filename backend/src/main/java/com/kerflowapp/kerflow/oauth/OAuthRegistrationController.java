package com.kerflowapp.kerflow.oauth;

import com.kerflowapp.kerflow.oauth.domain.ClientRegistrationRequest;
import com.kerflowapp.kerflow.oauth.domain.ClientRegistrationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Dynamic client registration (RFC 7591), unauthenticated by design: an MCP client has no
 * user context at registration time. This is what lets someone paste the /mcp URL into
 * Claude.ai and leave the client id/secret fields blank.
 */
@RestController
@RequiredArgsConstructor
public class OAuthRegistrationController {

    private final OAuthClientService clientService;

    @PostMapping(OAuthUrls.REGISTRATION_PATH)
    public ResponseEntity<ClientRegistrationResponse> register(@RequestBody ClientRegistrationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .header("Cache-Control", "no-store")
            .body(clientService.register(request));
    }

}
