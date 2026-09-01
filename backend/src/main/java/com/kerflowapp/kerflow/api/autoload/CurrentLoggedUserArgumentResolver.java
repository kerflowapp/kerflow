package com.kerflowapp.kerflow.api.autoload;

import com.kerflowapp.kerflow.domain.User;
import com.kerflowapp.kerflow.services.SecurityService;
import io.sentry.Sentry;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
@RequiredArgsConstructor
public class CurrentLoggedUserArgumentResolver extends AbstractArgumentResolver {

    private final SecurityService securityService;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().equals(User.class)
            && parameter.hasParameterAnnotation(CurrentLoggedUser.class);
    }
    
    @Override
    public Object resolveArgument(@NonNull MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  @NonNull NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {

        CurrentLoggedUser currentLoggedUserAnnotation = parameter.getParameterAnnotation(CurrentLoggedUser.class);

        if (currentLoggedUserAnnotation == null) {
            throw new RuntimeException("can not extract user id from params");
        }

        User loggedUser = securityService.getLoggedUser();
        Sentry.configureScope(scope -> {
            scope.setTag("login", loggedUser.getLogin());
        });
        return loggedUser;
    }

}
