package com.kerflowapp.kerflow.configuration.security;

import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.util.Assert;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class CustomJwtValidator implements OAuth2TokenValidator<Jwt> {

    @Override
    public OAuth2TokenValidatorResult validate(Jwt token) {
        Assert.notNull(token, "jwt cannot be null");
        Instant expiry = token.getExpiresAt();
        Instant issuedAt = token.getIssuedAt();
        if (expiry != null && issuedAt != null) {
            // sometimes token can expire due to an extra second delay
            // to avoid token expiration and have some margin, we add an extra minute
            Instant maxAllowedExpireDate = issuedAt.plus(1, ChronoUnit.HOURS).plus(1, ChronoUnit.MINUTES);
            if (maxAllowedExpireDate.isBefore(expiry)) {
                return OAuth2TokenValidatorResult.failure();
            }
        }
        return OAuth2TokenValidatorResult.success();
    }
}
