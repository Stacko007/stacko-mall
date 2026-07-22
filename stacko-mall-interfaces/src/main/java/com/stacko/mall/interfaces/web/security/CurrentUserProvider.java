package com.stacko.mall.interfaces.web.security;

public interface CurrentUserProvider {
    CurrentUser currentUser(String tenantId, String authorization);
}
