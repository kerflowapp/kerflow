package com.kerflowapp.kerflow.api.users;

import com.kerflowapp.kerflow.api.autoload.CurrentLoggedUser;
import com.kerflowapp.kerflow.api.users.domain.OAuthConnectionDto;
import com.kerflowapp.kerflow.domain.User;
import com.kerflowapp.kerflow.oauth.OAuthTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * The apps (Claude, ChatGPT…) the user connected through OAuth, and the way to disconnect them.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/users/me/oauth-connections")
public class OAuthConnectionController {

    private final OAuthTokenService oAuthTokenService;

    @GetMapping
    public ResponseEntity<List<OAuthConnectionDto>> getConnections(@CurrentLoggedUser User loggedUser) {
        List<OAuthConnectionDto> connections = oAuthTokenService.getConnections(loggedUser).stream()
            .map(connection -> OAuthConnectionDto.builder()
                .clientId(connection.clientId())
                .clientName(connection.clientName())
                .lastActivity(connection.lastActivity())
                .build())
            .toList();
        return ResponseEntity.ok(connections);
    }

    @DeleteMapping("/{client-id}")
    public ResponseEntity<Void> revokeConnection(@CurrentLoggedUser User loggedUser,
                                                 @PathVariable("client-id") UUID clientId) {

        oAuthTokenService.revokeGrant(loggedUser.getId(), clientId);
        return ResponseEntity.noContent().build();
    }

}
