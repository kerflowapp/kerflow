package com.kerflowapp.kerflow.api.users;

import com.kerflowapp.kerflow.api.autoload.CurrentLoggedUser;
import com.kerflowapp.kerflow.api.users.domain.UpdateUserRequest;
import com.kerflowapp.kerflow.domain.User;
import com.kerflowapp.kerflow.services.users.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/users/{user-id}")
public class UsersController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<Void> updateUser(@CurrentLoggedUser User loggedUser,
                                           @Valid @RequestBody UpdateUserRequest request) {
        
        userService.updateUser(loggedUser, request);
        return ResponseEntity.ok().build();
    }

}
