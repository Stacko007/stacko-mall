package com.stacko.mall.interfaces.web.security;

import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PermissionAspectTest {

    @AfterEach
    void resetRequestContext() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void passesRequestCredentialsAndPermissionToChecker() throws Throwable {
        AtomicReference<String> checked = new AtomicReference<>();
        PermissionChecker checker = (tenant, token, permission) ->
                checked.set(tenant + "|" + token + "|" + permission);
        PermissionAspect aspect = new PermissionAspect(checker);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Tenant-ID", "tenant-a");
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer token");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        Object result = aspect.checkPermission(joinPoint("ok"), permission());

        assertEquals("ok", result);
        assertEquals("tenant-a|Bearer token|mall:order:list", checked.get());
    }

    @Test
    void rejectsCallWithoutRequestContext() {
        PermissionAspect aspect = new PermissionAspect((tenant, token, permission) -> { });

        assertThrows(SecurityException.class,
                () -> aspect.checkPermission(joinPoint("ok"), permission()));
    }

    private ProceedingJoinPoint joinPoint(Object result) {
        return (ProceedingJoinPoint) Proxy.newProxyInstance(
                ProceedingJoinPoint.class.getClassLoader(),
                new Class<?>[]{ProceedingJoinPoint.class},
                (proxy, method, args) -> "proceed".equals(method.getName()) ? result : null);
    }

    private RequiresPermission permission() {
        try {
            return Fixture.class.getDeclaredMethod("secured").getAnnotation(RequiresPermission.class);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }

    private static class Fixture {
        @RequiresPermission("mall:order:list")
        void secured() {
        }
    }
}
