package com.stacko.mall.interfaces.web.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CurrentUserContextTest {

    @AfterEach
    void resetRequestContext() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void readsBoundIdentityWithinSameRequest() {
        CurrentUserContext context = new CurrentUserContext();
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        CurrentUser currentUser = new CurrentUser(
                3L, 7L, "alice", "tenant-a", java.util.Set.of("mall:product:list"));
        context.bind(request, currentUser);

        CurrentUser first = context.require("tenant-a");
        CurrentUser second = context.current();

        assertSame(first, second);
        assertEquals(3L, first.getAccountId());
        assertEquals(7L, first.getId());
        assertThrows(SecurityException.class,
                () -> context.require("tenant-b"));
    }

    @Test
    void rejectsRequestWithoutBoundIdentity() {
        CurrentUserContext context = new CurrentUserContext();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));

        assertThrows(SecurityException.class, () -> context.require("tenant-a"));
    }
}
