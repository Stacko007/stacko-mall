package com.stacko.mall.interfaces.web.security;

public interface PermissionChecker {
    void checkPermission(String tenantId, String authorization, String permissionCode);
}
