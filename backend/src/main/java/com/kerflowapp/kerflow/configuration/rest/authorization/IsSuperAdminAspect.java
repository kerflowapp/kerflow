package com.kerflowapp.kerflow.configuration.rest.authorization;


import com.kerflowapp.kerflow.services.SecurityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class IsSuperAdminAspect {

    private final ControllerGuardService controllerGuardService;
    private final SecurityService securityService;

    @Around("@annotation(isSuperAdmin)")
    public Object checkIsSuperAdmin(ProceedingJoinPoint joinPoint, IsSuperAdmin isSuperAdmin)
        throws Throwable {

        controllerGuardService.checkIsAdmin(securityService.getLoggedUser());

        return joinPoint.proceed();
    }

}
