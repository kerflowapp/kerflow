package com.kerflowapp.kerflow.mcp.auth;

import com.kerflowapp.kerflow.domain.User;
import com.kerflowapp.kerflow.oauth.OAuthUrls;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Authenticates /mcp requests with a personal API token (Authorization: Bearer kf_...).
 * On success the resolved User is exposed as a request attribute so the MCP transport
 * context extractor can carry it into tool executions (which may run on another thread).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class McpApiTokenFilter extends OncePerRequestFilter {

    public static final String MCP_USER_ATTRIBUTE = "kerflow.mcpUser";
    private static final String BEARER_PREFIX = "Bearer ";

    private final ApiTokenService apiTokenService;
    private final OAuthUrls oauthUrls;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/mcp");
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            reject(response);
            return;
        }

        Optional<User> user = apiTokenService.resolveUser(authorization.substring(BEARER_PREFIX.length()).trim());
        if (user.isEmpty()) {
            reject(response);
            return;
        }

        SecurityContextHolder.getContext().setAuthentication(
            UsernamePasswordAuthenticationToken.authenticated(
                user.get(), null, List.of(new SimpleGrantedAuthority("SCOPE_mcp"))));
        request.setAttribute(MCP_USER_ATTRIBUTE, user.get());

        filterChain.doFilter(request, response);
    }

    /**
     * The resource_metadata parameter (RFC 9728 section 5.1) is what makes OAuth discovery
     * work: it is the only thing pointing an MCP client at our authorization server.
     */
    private void reject(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setHeader("WWW-Authenticate",
            "Bearer resource_metadata=\"" + oauthUrls.protectedResourceMetadata() + "\"");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"error\":\"invalid_token\"}");
    }
}
