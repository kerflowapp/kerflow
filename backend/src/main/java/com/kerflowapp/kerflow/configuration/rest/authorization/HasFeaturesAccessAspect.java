package com.kerflowapp.kerflow.configuration.rest.authorization;


import com.kerflowapp.kerflow.domain.User;
import com.kerflowapp.kerflow.domain.enums.FeatureFlag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class HasFeaturesAccessAspect {

    private final ControllerGuardService controllerGuardService;

    @Around("@annotation(hasFeaturesAccess)")
    public Object checkHasFeatureAccess(ProceedingJoinPoint joinPoint, HasFeaturesAccess hasFeaturesAccess)
        throws Throwable {
        User user = getUser(joinPoint);

        for (FeatureFlag feature : hasFeaturesAccess.features()) {
            if (!controllerGuardService.hasFeaturesAccess(user, feature)) {
                throw new AccessDeniedException("User does not have access to feature " + feature);
            }
        }
        return joinPoint.proceed();
    }

    private static @NotNull User getUser(ProceedingJoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        User user = null;

        for (Object arg : args) {
            if (arg instanceof User) {
                user = (User) arg;
                break;
            }
        }

        if (user == null) {
            throw new RuntimeException("User parameter is missing");
        }
        return user;
    }

}
