package com.stacko.mall.interfaces.web.security;

import cn.dev33.satoken.stp.StpInterface;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Component
public class MallStpInterface implements StpInterface {
    private final CurrentUserContext currentUserContext;

    public MallStpInterface(CurrentUserContext currentUserContext) {
        this.currentUserContext = currentUserContext;
    }

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        CurrentUser current = currentUserContext.current();
        return values(current, loginId, loginType, current.getPermissions());
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        CurrentUser current = currentUserContext.current();
        return values(current, loginId, loginType, current.getRoles());
    }

    private List<String> values(CurrentUser current, Object loginId, String loginType, Set<String> values) {
        if (!UserCenterAuthProtocol.LOGIN_TYPE.equals(loginType)
                || current.getId() == null
                || !current.getId().toString().equals(String.valueOf(loginId))) {
            return Collections.emptyList();
        }
        return values == null ? Collections.emptyList() : new ArrayList<>(values);
    }
}
