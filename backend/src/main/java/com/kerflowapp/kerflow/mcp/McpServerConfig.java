package com.kerflowapp.kerflow.mcp;

import com.kerflowapp.kerflow.mcp.auth.McpApiTokenFilter;
import com.kerflowapp.kerflow.mcp.tools.AnalysisTools;
import com.kerflowapp.kerflow.mcp.tools.OutreachTools;
import com.kerflowapp.kerflow.mcp.tools.ProspectTools;
import com.kerflowapp.kerflow.mcp.tools.SearchTools;
import io.modelcontextprotocol.common.McpTransportContext;
import io.modelcontextprotocol.json.jackson3.JacksonMcpJsonMapper;
import org.springframework.ai.mcp.server.common.autoconfigure.properties.McpServerStreamableHttpProperties;
import org.springframework.ai.mcp.server.webmvc.transport.WebMvcStatelessServerTransport;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.json.JsonMapper;

import java.util.Map;

@Configuration
public class McpServerConfig {

    /**
     * Overrides the Spring AI auto-configured stateless transport to plug a context
     * extractor: it copies the User resolved by {@link McpApiTokenFilter} from the
     * request into the McpTransportContext, so tools can identify the caller even
     * when they execute outside the servlet thread.
     */
    @Bean
    public WebMvcStatelessServerTransport webMvcStatelessServerTransport(
        JsonMapper jsonMapper,
        McpServerStreamableHttpProperties properties) {
        return WebMvcStatelessServerTransport.builder()
            .jsonMapper(new JacksonMcpJsonMapper(jsonMapper))
            .messageEndpoint(properties.getMcpEndpoint())
            .contextExtractor(request -> {
                Object user = request.servletRequest().getAttribute(McpApiTokenFilter.MCP_USER_ATTRIBUTE);
                return user == null
                    ? McpTransportContext.EMPTY
                    : McpTransportContext.create(Map.of(McpApiTokenFilter.MCP_USER_ATTRIBUTE, user));
            })
            .build();
    }

    @Bean
    public ToolCallbackProvider kerflowTools(SearchTools searchTools,
                                             ProspectTools prospectTools,
                                             AnalysisTools analysisTools,
                                             OutreachTools outreachTools) {
        return MethodToolCallbackProvider.builder()
            .toolObjects(searchTools, prospectTools, analysisTools, outreachTools)
            .build();
    }

    /**
     * The filter is applied by the /mcp security chain; disable the servlet
     * container auto-registration so it does not run twice.
     */
    @Bean
    public FilterRegistrationBean<McpApiTokenFilter> mcpApiTokenFilterRegistration(McpApiTokenFilter filter) {
        FilterRegistrationBean<McpApiTokenFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }
}
