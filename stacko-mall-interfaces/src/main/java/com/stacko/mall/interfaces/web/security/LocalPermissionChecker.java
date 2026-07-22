package com.stacko.mall.interfaces.web.security;

import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.stp.StpLogic;
import org.springframework.stereotype.Component;

@Component
public class LocalPermissionChecker implements PermissionChecker {
    private final CurrentUserContext currentUserContext;
    private final StpLogic stpLogic;

    public LocalPermissionChecker(CurrentUserContext currentUserContext, StpLogic stpLogic) {
        this.currentUserContext = currentUserContext;
        this.stpLogic = stpLogic;
    }

    @Override
    public void checkPermission(String tenantId, String authorization, String permissionCode) {
        CurrentUser currentUser = currentUserContext.require(tenantId, authorization);
        if (!stpLogic.hasPermission(currentUser.getId(), permissionCode)) {
            throw new NotPermissionException(permissionCode, UserCenterAuthProtocol.LOGIN_TYPE);
        }
    }
}
