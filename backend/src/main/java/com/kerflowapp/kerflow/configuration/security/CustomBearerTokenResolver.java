package com.kerflowapp.kerflow.configuration.security;

import com.nimbusds.oauth2.sdk.util.StringUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

@Slf4j
@Component
public class CustomBearerTokenResolver implements BearerTokenResolver {

    public static final String AUTH_COOKIE_NAME = "kerflow-bearer";
    private final DefaultBearerTokenResolver defaultBearerTokenResolver = new DefaultBearerTokenResolver();
    private final Set<String> unsecuredPaths;

    public CustomBearerTokenResolver(Set<String> unsecuredPaths) {
        this.unsecuredPaths = unsecuredPaths;
    }

    @Override
    public String resolve(HttpServletRequest request) {
        String token = defaultBearerTokenResolver.resolve(request);
        if (token == null) {
            return getBearerFromCookies(request).orElse(null);
        }
        return token;
    }

    private Optional<String> getBearerFromCookies(HttpServletRequest request) {
        // get token from a Cookie
        Cookie[] cookies = request.getCookies();

        if (cookies == null || cookies.length < 1) {
            return Optional.empty();
        }

        if (unsecuredPaths.stream().anyMatch(path -> antMatcher(path).matches(request))) {
            return Optional.empty();
        }

        // would be a lot better if we can check HttpOnly and Secure flags are set
        // but the info is not retrieved
        return Arrays.stream(cookies)
            .filter(cookie -> AUTH_COOKIE_NAME.equals(cookie.getName()))
            .findFirst()
            .map(Cookie::getValue)
            .filter(StringUtils::isNotBlank);
    }
}
