package com.stacko.mall.interfaces.web.security;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.stp.StpInterfaceDefaultImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LocalPermissionCheckerTest {

    @AfterEach
    void resetContext() {
        RequestContextHolder.resetRequestAttributes();
        SaManager.setStpInterface(new StpInterfaceDefaultImpl());
    }

    @Test
    void allowsExactPermissionAndRejectsMissingPermission() {
        CurrentUserProvider provider = (tenantId, authorization) -> user(
                tenantId, Set.of("mall:order:list", "mall:order:read"));
        LocalPermissionChecker checker = checker(provider);

        assertDoesNotThrow(() -> checker.checkPermission(
                "tenant-a", "Bearer token", "mall:order:list"));
        assertThrows(NotPermissionException.class, () -> checker.checkPermission(
                "tenant-a", "Bearer token", "mall:order:ship"));
    }

    @Test
    void doesNotApplyWildcardOrPrefixMatching() {
        CurrentUserProvider provider = (tenantId, authorization) -> user(
                tenantId, Set.of("mall:order:*", "mall"));
        LocalPermissionChecker checker = checker(provider);

        assertThrows(NotPermissionException.class, () -> checker.checkPermission(
                "tenant-a", "Bearer token", "mall:order:list"));
    }

    private CurrentUser user(String tenantId, Set<String> permissions) {
        CurrentUser currentUser = new CurrentUser();
        currentUser.setId(7L);
        currentUser.setTenantId(tenantId);
        currentUser.setPermissions(permissions);
        return currentUser;
    }

    private LocalPermissionChecker checker(CurrentUserProvider provider) {
        CurrentUserContext context = new CurrentUserContext(provider);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));
        SaManager.setStpInterface(new MallStpInterface(context));
        return new LocalPermissionChecker(context, new SaTokenIdentityConfiguration().stackoUserStpLogic());
    }
}
