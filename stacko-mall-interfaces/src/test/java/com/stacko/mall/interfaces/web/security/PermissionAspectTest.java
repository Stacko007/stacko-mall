package com.stacko.mall.interfaces.web.security;

import com.stacko.mall.interfaces.web.admin.PermissionAspectFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PermissionAspectTest {
    private final CurrentUserContext currentUserContext = new CurrentUserContext();
    private final PermissionAspect aspect = new PermissionAspect(currentUserContext);

    @AfterEach
    void resetRequestContext() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void allowsMatchingPermission() {
        bind(Set.of("mall:product:list"));

        assertEquals("ok", proxy().list());
    }

    @Test
    void deniesMissingPermission() {
        bind(Set.of("mall:order:list"));

        assertThrows(SecurityException.class, () -> proxy().list());
    }

    @Test
    void deniesAdminMethodWithoutPermissionAnnotation() {
        bind(Set.of("mall:product:list"));

        assertThrows(SecurityException.class, () -> proxy().notConfigured());
    }

    private void bind(Set<String> permissions) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        currentUserContext.bind(request,
                new CurrentUser(3L, 7L, "alice", "tenant-a", permissions));
    }

    private PermissionAspectFixture proxy() {
        AspectJProxyFactory factory = new AspectJProxyFactory(new PermissionAspectFixture());
        factory.setProxyTargetClass(true);
        factory.addAspect(aspect);
        return factory.getProxy();
    }
}
