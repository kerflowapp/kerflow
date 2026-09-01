package com.kerflowapp.kerflow.api.users;

import com.kerflowapp.kerflow.api.autoload.CurrentLoggedUser;
import com.kerflowapp.kerflow.api.users.domain.ApiTokenDto;
import com.kerflowapp.kerflow.api.users.domain.CreateApiTokenRequest;
import com.kerflowapp.kerflow.api.users.domain.CreatedApiTokenDto;
import com.kerflowapp.kerflow.domain.User;
import com.kerflowapp.kerflow.mcp.auth.ApiTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/users/me/api-tokens")
public class ApiTokenController {

    private final ApiTokenService apiTokenService;

    @PostMapping
    public ResponseEntity<CreatedApiTokenDto> createToken(@CurrentLoggedUser User loggedUser,
                                                          @Valid @RequestBody CreateApiTokenRequest request) {

        ApiTokenService.CreatedToken created = apiTokenService.createToken(loggedUser, request.name());
        return ResponseEntity.ok(CreatedApiTokenDto.builder()
            .id(created.apiToken().getId())
            .name(created.apiToken().getName())
            .tokenPrefix(created.apiToken().getTokenPrefix())
            .token(created.plaintext())
            .build());
    }

    @GetMapping
    public ResponseEntity<List<ApiTokenDto>> getTokens(@CurrentLoggedUser User loggedUser) {
        List<ApiTokenDto> tokens = apiTokenService.getTokens(loggedUser).stream()
            .map(token -> ApiTokenDto.builder()
                .id(token.getId())
                .name(token.getName())
                .tokenPrefix(token.getTokenPrefix())
                .creationDate(token.getCreationDate())
                .lastUsedAt(token.getLastUsedAt())
                .revokedAt(token.getRevokedAt())
                .build())
            .toList();
        return ResponseEntity.ok(tokens);
    }

    @DeleteMapping("/{token-id}")
    public ResponseEntity<Void> revokeToken(@CurrentLoggedUser User loggedUser,
                                            @PathVariable("token-id") UUID tokenId) {

        apiTokenService.revokeToken(loggedUser, tokenId);
        return ResponseEntity.noContent().build();
    }

}
