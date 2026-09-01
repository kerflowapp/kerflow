package com.kerflowapp.kerflow.api.oauth;

import com.kerflowapp.kerflow.api.autoload.CurrentLoggedUser;
import com.kerflowapp.kerflow.domain.OAuthAuthorizationRequest;
import com.kerflowapp.kerflow.domain.User;
import com.kerflowapp.kerflow.oauth.OAuthAuthorizationService;
import com.kerflowapp.kerflow.oauth.domain.AuthorizationRequestDto;
import com.kerflowapp.kerflow.oauth.domain.ConsentDecisionDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * The consent screen's backend, behind the normal Cognito JWT chain: this is where the
 * user's identity actually enters the OAuth flow. The /oauth2 endpoints themselves are
 * anonymous and never learn who the user is.
 *
 * <p>No subscription check here on purpose — access is enforced per tool call by
 * McpUserContext.requireActiveAccess, so a lapsed user can still connect and see why.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(OAuthConsentController.BASE_PATH)
public class OAuthConsentController {

    public static final String BASE_PATH = "/v1/oauth2/authorization-requests";

    private final OAuthAuthorizationService authorizationService;

    @GetMapping("/{request-id}")
    public ResponseEntity<AuthorizationRequestDto> getRequest(@PathVariable("request-id") UUID requestId) {
        OAuthAuthorizationRequest request = authorizationService.requireUsableRequest(requestId);

        return ResponseEntity.ok(AuthorizationRequestDto.builder()
            .clientName(request.getClient().getClientName())
            .scopes(splitScopes(request.getScope()))
            .expiresAt(request.getExpiresAt())
            .build());
    }

    @PostMapping("/{request-id}/approve")
    public ResponseEntity<ConsentDecisionDto> approve(@CurrentLoggedUser User loggedUser,
                                                      @PathVariable("request-id") UUID requestId) {

        OAuthAuthorizationService.IssuedCode issued = authorizationService.approve(requestId, loggedUser);
        return ResponseEntity.ok(ConsentDecisionDto.builder().redirectUri(issued.redirectUri()).build());
    }

    @PostMapping("/{request-id}/deny")
    public ResponseEntity<ConsentDecisionDto> deny(@CurrentLoggedUser User loggedUser,
                                                   @PathVariable("request-id") UUID requestId) {
        
        return ResponseEntity.ok(ConsentDecisionDto.builder()
            .redirectUri(authorizationService.deny(requestId))
            .build());
    }

    private List<String> splitScopes(String scope) {
        if (scope == null || scope.isBlank()) {
            return List.of();
        }
        return Arrays.stream(scope.split(" ")).filter(value -> !value.isBlank()).toList();
    }
}
