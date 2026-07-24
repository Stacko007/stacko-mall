package com.stacko.mall.interfaces.web.security;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Component
public class PermissionAspect {
    private final CurrentUserContext currentUserContext;

    public PermissionAspect(CurrentUserContext currentUserContext) {
        this.currentUserContext = currentUserContext;
    }

    @Around("execution(public * com.stacko.mall.interfaces.web.admin..*(..))")
    public Object checkPermission(ProceedingJoinPoint joinPoint) throws Throwable {
        RequiresPermission requirement = findRequirement(joinPoint);
        if (requirement == null || !currentUserContext.current().hasPermission(requirement.value())) {
            throw new SecurityException("Forbidden");
        }
        return joinPoint.proceed();
    }

    private RequiresPermission findRequirement(ProceedingJoinPoint joinPoint) {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Class<?> targetClass = joinPoint.getTarget() == null
                ? method.getDeclaringClass() : AopUtils.getTargetClass(joinPoint.getTarget());
        Method targetMethod = AopUtils.getMostSpecificMethod(method, targetClass);
        RequiresPermission requirement = AnnotatedElementUtils.findMergedAnnotation(
                targetMethod, RequiresPermission.class);
        if (requirement != null) {
            return requirement;
        }
        return AnnotatedElementUtils.findMergedAnnotation(targetClass, RequiresPermission.class);
    }
}
