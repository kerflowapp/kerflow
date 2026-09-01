package com.kerflowapp.kerflow.oauth;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * An error to be rendered in the OAuth wire format ({@code {"error": ..., "error_description": ...}}),
 * which is not the shape of {@code ApiError} used by the REST API. Thrown by the public
 * /oauth2 endpoints, handled by {@link OAuthExceptionHandler}.
 */
@Getter
public class OAuthException extends RuntimeException {

    private final String error;
    private final String errorDescription;
    private final HttpStatus status;

    public OAuthException(String error, String errorDescription, HttpStatus status) {
        super(error + ": " + errorDescription);
        this.error = error;
        this.errorDescription = errorDescription;
        this.status = status;
    }

    public static OAuthException invalidRequest(String description) {
        return new OAuthException("invalid_request", description, HttpStatus.BAD_REQUEST);
    }

    public static OAuthException invalidClient(String description) {
        return new OAuthException("invalid_client", description, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Covers a bad, expired, replayed or mismatched code / refresh token. Stays deliberately vague.
     */
    public static OAuthException invalidGrant(String description) {
        return new OAuthException("invalid_grant", description, HttpStatus.BAD_REQUEST);
    }

    public static OAuthException unsupportedGrantType(String description) {
        return new OAuthException("unsupported_grant_type", description, HttpStatus.BAD_REQUEST);
    }

    public static OAuthException invalidClientMetadata(String description) {
        return new OAuthException("invalid_client_metadata", description, HttpStatus.BAD_REQUEST);
    }
}
