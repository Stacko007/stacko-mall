package com.stacko.mall.interfaces.web.security;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class PermissionAspect {
    private final PermissionChecker permissionChecker;

    public PermissionAspect(PermissionChecker permissionChecker) {
        this.permissionChecker = permissionChecker;
    }

    @Around("@annotation(required)")
    public Object checkPermission(ProceedingJoinPoint pjp, RequiresPermission required) throws Throwable {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            throw new SecurityException("No request context");
        }
        HttpServletRequest request = attrs.getRequest();
        String tenantId = request.getHeader("X-Tenant-ID");
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        permissionChecker.checkPermission(tenantId, authorization, required.value());
        return pjp.proceed();
    }
}
