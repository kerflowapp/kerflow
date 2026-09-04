package com.kerflowapp.kerflow.configuration.security;

import com.kerflowapp.kerflow.api.authentication.AuthenticationController;
import com.kerflowapp.kerflow.mcp.auth.McpApiTokenFilter;
import io.micrometer.common.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;
import java.util.Set;

import static com.kerflowapp.kerflow.api.authentication.AuthenticationController.*;
import static org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher.pathPattern;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity()
public class SecurityConfig {

    public static final Logger LOGGER = LoggerFactory.getLogger(SecurityConfig.class);

    private static List<String> getAllowedOrigins(String allowedOrigin) {
        if (StringUtils.isBlank(allowedOrigin) || "*".equals(allowedOrigin)) {
            throw new IllegalArgumentException("Cors '" + allowedOrigin + "' allowed origins is not allowed");
        }
        return List.of(allowedOrigin.split(","));
    }

    @Bean
    protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
        return new RegisterSessionAuthenticationStrategy(new SessionRegistryImpl());
    }

    /**
     * OAuth discovery, dynamic client registration and the authorization code flow.
     * These are anonymous by definition: a client discovers and registers before any user
     * is involved, and the token endpoint authenticates the client, not the user. The user
     * only ever enters the flow through the consent endpoints, which sit on the JWT chain.
     * Claimed first so the catch-all authenticated() rule below cannot swallow them.
     */
    @Bean
    @Order(0)
    public SecurityFilterChain oauthFilterChain(HttpSecurity http) throws Exception {

        http.securityMatcher("/.well-known/**", "/oauth2/**")
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(publicEndpointsCorsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());

        return http.build();
    }

    /**
     * /mcp is authenticated by API token (personal, or minted by the OAuth flow), not by
     * a Cognito JWT. Claimed before the resource-server chain so it never sees it.
     */
    @Bean
    @Order(1)
    public SecurityFilterChain mcpFilterChain(HttpSecurity http, McpApiTokenFilter mcpApiTokenFilter)
        throws Exception {

        http.securityMatcher("/mcp/**", "/mcp")
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(mcpApiTokenFilter, AnonymousAuthenticationFilter.class)
            .authorizeHttpRequests(auth -> auth.anyRequest().authenticated());

        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain resourceServerFilterChain(HttpSecurity http,
                                                         CorsConfigurationSource corsConfigurationSource)
        throws Exception {

        Set<String> unsecuredPaths = Set.of(
            AuthenticationController.BASE_PATH + SIGNIN_PATH,
            AuthenticationController.BASE_PATH + SIGNUP_PATH,
            AuthenticationController.BASE_PATH + REFRESH_PATH,
            AuthenticationController.BASE_PATH + RESET_PASSWORD_PATH,
            AuthenticationController.BASE_PATH + SIGNUP_CONFIRMATION_PATH + "/**",
            AuthenticationController.BASE_PATH + CHECK_EMAIL_PATH,
            AuthenticationController.BASE_PATH + VERIFY_PATH,
            AuthenticationController.BASE_PATH + RESEND_VERIFICATION_PATH,
            "/health",
            "/actuator/**"
        );

        http.csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .authorizeHttpRequests(auth ->
                unsecuredPaths.forEach(path -> auth.requestMatchers(pathPattern(path)).permitAll())
            )
            .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
            .oauth2ResourceServer(w -> w
                .jwt(Customizer.withDefaults())
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(cust -> cust
                .accessDeniedHandler(new RestAccessDeniedHandler())
            );

        return http.build();
    }

    /**
     * The OAuth discovery and flow endpoints are public and carry no cookie or ambient
     * authority, so any origin may read them — which browser-based MCP clients need.
     * Deliberately not credentialed, and not registered as a bean so it cannot leak onto
     * another chain.
     */
    private CorsConfigurationSource publicEndpointsCorsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("authorization", "content-type", "accept"));
        configuration.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    @Bean
    @Primary
    public CorsConfigurationSource corsConfigurationSource(@Value("${allowed-origin}") String allowedOrigin) {
        LOGGER.info("using '{}' for cors allowed origins", allowedOrigin);
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(getAllowedOrigins(allowedOrigin));
        configuration.setAllowedMethods(List.of("*"));
        configuration.setAllowedHeaders(List.of("authorization", "content-type", "accept"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

}
