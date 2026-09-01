package com.kerflowapp.kerflow.mcp.auth;

import com.kerflowapp.kerflow.configuration.rest.authorization.ControllerGuardService;
import com.kerflowapp.kerflow.domain.User;
import com.kerflowapp.kerflow.exceptions.KerflowException;
import io.modelcontextprotocol.common.McpTransportContext;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.mcp.McpToolUtils;
import org.springframework.stereotype.Component;

import static com.kerflowapp.kerflow.exceptions.KerflowException.ErrorCode.UNAUTHORIZED;

/**
 * Resolves the authenticated User inside an MCP tool execution.
 * Tool calls may run outside the servlet thread, so the User travels in the
 * McpTransportContext (populated by the transport's context extractor from the
 * request attribute set by {@link McpApiTokenFilter}), not in a ThreadLocal.
 */
@Component
@RequiredArgsConstructor
public class McpUserContext {

    private final ControllerGuardService controllerGuardService;

    public User currentUser(ToolContext toolContext) {
        Object exchange = toolContext != null
            ? toolContext.getContext().get(McpToolUtils.TOOL_CONTEXT_MCP_EXCHANGE_KEY)
            : null;
        if (exchange instanceof McpTransportContext transportContext
            && transportContext.get(McpApiTokenFilter.MCP_USER_ATTRIBUTE) instanceof User user) {
            return user;
        }
        throw new IllegalStateException("No authenticated user in MCP tool context");
    }

    /**
     * Same access rule as the REST @RequiresSubscription aspect (active subscription or trial).
     */
    public User requireActiveAccess(ToolContext toolContext) {
        User user = currentUser(toolContext);
        if (!controllerGuardService.hasActiveAccess(user)) {
            throw new KerflowException(UNAUTHORIZED,
                "Subscription or trial expired: this Kerflow account has no active access");
        }
        return user;
    }

}
