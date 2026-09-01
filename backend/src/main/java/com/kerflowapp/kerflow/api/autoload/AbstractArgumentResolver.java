package com.kerflowapp.kerflow.api.autoload;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.HandlerMapping;

import java.util.Map;

public abstract class AbstractArgumentResolver implements HandlerMethodArgumentResolver {

    @SuppressWarnings("unchecked")
    protected Map<String, String> getPathVariables(NativeWebRequest webRequest) {
        return (Map<String, String>) webRequest
            .getNativeRequest(HttpServletRequest.class)
            .getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
    }

    @SuppressWarnings("unchecked")
    protected Map<String, String[]> getRequestParams(NativeWebRequest webRequest) {
        return (Map<String, String[]>) webRequest
            .getNativeRequest(HttpServletRequest.class)
            .getParameterMap();
    }
}
