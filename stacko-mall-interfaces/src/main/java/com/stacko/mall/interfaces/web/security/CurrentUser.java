package com.stacko.mall.interfaces.web.security;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class CurrentUser {
    private Long id;
    private String username;
    private String phone;
    private String email;
    private String status;
    private String tenantId;
    private Set<String> roles;
    private Set<String> permissions;

    public String userIdAsString() {
        if (id == null) {
            throw new SecurityException("Current user id missing");
        }
        return String.valueOf(id);
    }
}
