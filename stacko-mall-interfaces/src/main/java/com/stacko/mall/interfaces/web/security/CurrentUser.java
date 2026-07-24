package com.stacko.mall.interfaces.web.security;

import lombok.Getter;

import java.util.Set;

@Getter
public class CurrentUser {
    private final Long accountId;
    private final Long id;
    private final String username;
    private final String tenantId;
    private final Set<String> permissions;

    public CurrentUser(Long accountId,
                       Long membershipId,
                       String username,
                       String tenantId,
                       Set<String> permissions) {
        this.accountId = accountId;
        this.id = membershipId;
        this.username = username;
        this.tenantId = tenantId;
        this.permissions = permissions == null ? Set.of() : Set.copyOf(permissions);
    }

    public boolean hasPermission(String permission) {
        return permissions.contains(permission);
    }

    public String getPhone() {
        return null;
    }

    public String getEmail() {
        return null;
    }

}
