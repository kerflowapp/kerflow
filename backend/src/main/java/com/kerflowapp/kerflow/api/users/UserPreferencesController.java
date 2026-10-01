package com.kerflowapp.kerflow.api.users;

import com.kerflowapp.kerflow.api.autoload.CurrentLoggedUser;
import com.kerflowapp.kerflow.api.users.domain.UpdatePreferencesRequest;
import com.kerflowapp.kerflow.domain.User;
import com.kerflowapp.kerflow.services.users.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/users/me/preferences")
public class UserPreferencesController {

    private final UserService userService;

    @PutMapping
    public ResponseEntity<Void> updatePreferences(@CurrentLoggedUser User loggedUser,
                                                  @Valid @RequestBody UpdatePreferencesRequest request) {

        userService.updatePreferences(loggedUser, request);
        return ResponseEntity.ok().build();
    }
}
