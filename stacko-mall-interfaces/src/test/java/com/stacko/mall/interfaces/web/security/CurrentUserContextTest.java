package com.stacko.mall.interfaces.web.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CurrentUserContextTest {

    @AfterEach
    void resetRequestContext() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void cachesCurrentUserWithinSameRequest() {
        AtomicInteger calls = new AtomicInteger();
        CurrentUserProvider provider = (tenantId, authorization) -> {
            calls.incrementAndGet();
            CurrentUser currentUser = new CurrentUser();
            currentUser.setId(7L);
            currentUser.setTenantId(tenantId);
            return currentUser;
        };
        CurrentUserContext context = new CurrentUserContext(provider);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));

        CurrentUser first = context.require("tenant-a", "Bearer token");
        CurrentUser second = context.require("tenant-a", "Bearer token");

        assertSame(first, second);
        assertEquals(1, calls.get());
        assertThrows(SecurityException.class,
                () -> context.require("tenant-b", "Bearer token"));
    }
}
