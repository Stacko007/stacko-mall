package com.stacko.mall.interfaces.web.security;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpInterfaceDefaultImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SaTokenCurrentUserProviderTest {

    @AfterEach
    void resetContext() {
        RequestContextHolder.resetRequestAttributes();
        SaManager.setStpInterface(new StpInterfaceDefaultImpl());
    }

    @Test
    void resolvesCurrentUserFromSharedSaSession() {
        FakeStpLogic stpLogic = new FakeStpLogic();
        stpLogic.add("valid", 7L, session("tenant-a", "ACTIVE"));
        SaTokenCurrentUserProvider provider = new SaTokenCurrentUserProvider(stpLogic);

        CurrentUser currentUser = provider.currentUser("tenant-a", "Bearer valid");

        assertEquals(7L, currentUser.getId());
        assertEquals("alice", currentUser.getUsername());
        assertEquals("tenant-a", currentUser.getTenantId());
        assertEquals(java.util.Set.of("mall:order:list"), currentUser.getPermissions());
    }

    @Test
    void rejectsMissingOrMalformedToken() {
        SaTokenCurrentUserProvider provider = new SaTokenCurrentUserProvider(new FakeStpLogic());

        assertThrows(SecurityException.class, () -> provider.currentUser("tenant-a", null));
        assertThrows(SecurityException.class, () -> provider.currentUser("tenant-a", "valid"));
        assertThrows(SecurityException.class, () -> provider.currentUser("tenant-a", "Bearer missing"));
    }

    @Test
    void rejectsCrossTenantAndDisabledSession() {
        FakeStpLogic stpLogic = new FakeStpLogic();
        stpLogic.add("cross", 7L, session("tenant-b", "ACTIVE"));
        stpLogic.add("disabled", 8L, session("tenant-a", "DISABLED"));
        SaTokenCurrentUserProvider provider = new SaTokenCurrentUserProvider(stpLogic);

        assertThrows(SecurityException.class,
                () -> provider.currentUser("tenant-a", "Bearer cross"));
        assertThrows(SecurityException.class,
                () -> provider.currentUser("tenant-a", "Bearer disabled"));
    }

    @Test
    void permissionCheckFailsAfterSharedSessionIsRevoked() {
        FakeStpLogic stpLogic = new FakeStpLogic();
        stpLogic.add("valid", 7L, session("tenant-a", "ACTIVE"));
        CurrentUserContext context = new CurrentUserContext(new SaTokenCurrentUserProvider(stpLogic));
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));
        SaManager.setStpInterface(new MallStpInterface(context));
        LocalPermissionChecker checker = new LocalPermissionChecker(context, stpLogic);

        assertDoesNotThrow(() -> checker.checkPermission(
                "tenant-a", "Bearer valid", "mall:order:list"));

        stpLogic.remove("valid");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));

        assertThrows(SecurityException.class, () -> checker.checkPermission(
                "tenant-a", "Bearer valid", "mall:order:list"));
    }

    private FakeSession session(String tenantId, String status) {
        FakeSession session = new FakeSession();
        session.set("tenantId", tenantId);
        session.set("username", "alice");
        session.set("phone", "13800000000");
        session.set("email", "alice@example.com");
        session.set("status", status);
        session.set("roles", List.of("member"));
        session.set("permissions", List.of("mall:order:list"));
        return session;
    }

    private static class FakeStpLogic extends cn.dev33.satoken.stp.StpLogic {
        private final Map<String, Object> loginIds = new ConcurrentHashMap<>();
        private final Map<String, SaSession> sessions = new ConcurrentHashMap<>();

        private FakeStpLogic() {
            super(UserCenterAuthProtocol.LOGIN_TYPE);
        }

        private void add(String token, Object loginId, SaSession session) {
            loginIds.put(token, loginId);
            sessions.put(token, session);
        }

        private void remove(String token) {
            loginIds.remove(token);
            sessions.remove(token);
        }

        @Override
        public Object getLoginIdByToken(String token) {
            return loginIds.get(token);
        }

        @Override
        public SaSession getTokenSessionByToken(String token) {
            return sessions.get(token);
        }
    }

    private static class FakeSession extends SaSession {
        private final Map<String, Object> values = new ConcurrentHashMap<>();

        @Override
        public SaSession set(String key, Object value) {
            if (value != null) {
                values.put(key, value);
            }
            return this;
        }

        @Override
        public Object get(String key) {
            return values.get(key);
        }
    }
}
