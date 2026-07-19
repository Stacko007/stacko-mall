package com.stacko.mall.interfaces.web.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Setter
@Getter
@ConfigurationProperties(prefix = "mall.user-center")
public class UserCenterProperties {
    private boolean enabled = true;
    private String baseUrl = "http://localhost:8080";
    private String aclCheckPath = "/api/acl/check";
    private String currentUserPath = "/api/auth/current";

}
