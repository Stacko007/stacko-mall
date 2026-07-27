package com.stacko.mall.interfaces.web.security;

import lombok.Getter;

import java.util.Set;

@Getter
public class CurrentUser {
    private final Long accountId;
    private final Long id;
    private final String username;
    private final String tenantId;
    private final String applicationCode;
    private final String portalCode;
    private final String audience;
    private final Set<String> permissions;

    public CurrentUser(Long accountId,
                       Long membershipId,
                       String username,
                       String tenantId,
                       String applicationCode,
                       String portalCode,
                       String audience,
                       Set<String> permissions) {
        this.accountId = accountId;
        this.id = membershipId;
        this.username = username;
        this.tenantId = tenantId;
        this.applicationCode = applicationCode;
        this.portalCode = portalCode;
        this.audience = audience;
        this.permissions = permissions == null ? Set.of() : Set.copyOf(permissions);
    }

    public CurrentUser(Long accountId,
                       Long membershipId,
                       String username,
                       String tenantId,
                       Set<String> permissions) {
        this(accountId, membershipId, username, tenantId, null, null, null, permissions);
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
