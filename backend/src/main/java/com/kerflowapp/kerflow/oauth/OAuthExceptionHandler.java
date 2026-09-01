package com.kerflowapp.kerflow.oauth;

import com.kerflowapp.kerflow.api.oauth.OAuthConsentController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Scoped to the OAuth controllers so the REST API keeps its own {@code ApiError} format.
 *
 * <p>Ordered first because {@code CustomExceptionHandler} is a global advice handling
 * RuntimeException, which {@link OAuthException} is: without this, OAuth errors would be
 * swallowed into a generic 500 and clients would see no error code at all.
 */
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(assignableTypes = {
    OAuthAuthorizationController.class,
    OAuthRegistrationController.class,
    OAuthConsentController.class
})
public class OAuthExceptionHandler {

    @ExceptionHandler(OAuthException.class)
    public ResponseEntity<Map<String, String>> handle(OAuthException exception) {
        LOGGER.warn("OAuth error: {} - {}", exception.getError(), exception.getErrorDescription());

        Map<String, String> body = new LinkedHashMap<>();
        body.put("error", exception.getError());
        body.put("error_description", exception.getErrorDescription());
        return ResponseEntity.status(exception.getStatus())
            .header("Cache-Control", "no-store")
            .body(body);
    }

}
