package com.kerflowapp.kerflow.services;

import com.kerflowapp.kerflow.domain.User;
import com.kerflowapp.kerflow.exceptions.KerflowException;
import com.kerflowapp.kerflow.services.users.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminGetUserResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType;

import java.util.Collection;
import java.util.List;

import static com.kerflowapp.kerflow.exceptions.KerflowException.ErrorCode.USER_NOT_ENABLED;
import static com.kerflowapp.kerflow.exceptions.KerflowException.ErrorCode.USER_NOT_FOUND;
import static java.util.Optional.ofNullable;

@Service
@RequiredArgsConstructor
public class SecurityService {

    private final CognitoService cognitoService;
    private final UserService userService;

    public User getLoggedUser() {
        SecurityContext context = SecurityContextHolder.getContext();
        if (context == null || context.getAuthentication() == null) {
            return null;
        }
        Object principal = context.getAuthentication().getPrincipal();
        if (!(principal instanceof Jwt jwtPrincipal)) {
            return null;
        }

        String cognitoUsername = jwtPrincipal.getClaim("username");
        AdminGetUserResponse cognitoUser = cognitoService.getUser(cognitoUsername);

        List<AttributeType> userAttributes = cognitoUser.userAttributes();
        if (!cognitoUser.enabled()) {
            throw new KerflowException(USER_NOT_ENABLED);
        }

        String login = ofNullable(userAttributes)
            .stream()
            .flatMap(Collection::stream)
            .filter(attributeType -> attributeType.name().equals("email"))
            .map(AttributeType::value)
            .findFirst()
            .orElseThrow(() -> new KerflowException(USER_NOT_FOUND));

        return userService.getUserFromLogin(login).orElseThrow(() -> new KerflowException(USER_NOT_FOUND));
    }
}
