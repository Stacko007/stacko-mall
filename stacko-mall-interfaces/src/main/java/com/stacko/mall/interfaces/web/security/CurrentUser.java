package com.stacko.mall.interfaces.web.security;

import lombok.Getter;

@Getter
public class CurrentUser {
    private final Long accountId;
    private final Long id;
    private final String username;
    private final String tenantId;

    public CurrentUser(Long accountId, Long membershipId, String username, String tenantId) {
        this.accountId = accountId;
        this.id = membershipId;
        this.username = username;
        this.tenantId = tenantId;
    }

    public String getPhone() {
        return null;
    }

    public String getEmail() {
        return null;
    }

}
