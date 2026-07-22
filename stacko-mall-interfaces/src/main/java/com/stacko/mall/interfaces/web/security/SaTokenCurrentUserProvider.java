package com.stacko.mall.interfaces.web.security;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpLogic;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Component
public class SaTokenCurrentUserProvider implements CurrentUserProvider {
    private final StpLogic stpLogic;

    public SaTokenCurrentUserProvider(StpLogic stpLogic) {
        this.stpLogic = stpLogic;
    }

    @Override
    public CurrentUser currentUser(String tenantId, String authorization) {
        requireText(tenantId, "Tenant required");
        String token = extractBearerToken(authorization);
        try {
            Object loginId = stpLogic.getLoginIdByToken(token);
            if (loginId == null) {
                throw new SecurityException("Unauthorized");
            }
            SaSession session = stpLogic.getTokenSessionByToken(token);
            if (session == null) {
                throw new SecurityException("Unauthorized");
            }
            String sessionTenantId = stringValue(session.get("tenantId"));
            if (!tenantId.equals(sessionTenantId)) {
                throw new SecurityException("Tenant mismatch");
            }
            String status = stringValue(session.get("status"));
            if (status != null && !"ACTIVE".equals(status)) {
                throw new SecurityException("Unauthorized");
            }

            CurrentUser currentUser = new CurrentUser();
            currentUser.setId(asLong(loginId));
            currentUser.setUsername(stringValue(session.get("username")));
            currentUser.setStatus(status == null ? "ACTIVE" : status);
            currentUser.setTenantId(sessionTenantId);
            currentUser.setRoles(asStringSet(session.get("roles")));
            currentUser.setPermissions(asStringSet(session.get("permissions")));
            return currentUser;
        } catch (SecurityException e) {
            throw e;
        } catch (Exception e) {
            throw new AuthenticationServiceUnavailableException("Authentication service unavailable", e);
        }
    }

    private String extractBearerToken(String authorization) {
        requireText(authorization, "Unauthorized");
        if (!authorization.regionMatches(true, 0, "Bearer ", 0, 7)) {
            throw new SecurityException("Unauthorized");
        }
        String token = authorization.substring(7).trim();
        requireText(token, "Unauthorized");
        return token;
    }

    private void requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new SecurityException(message);
        }
    }

    private Long asLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(value.toString());
    }

    private String stringValue(Object value) {
        return value == null ? null : value.toString();
    }

    private Set<String> asStringSet(Object value) {
        if (value == null) {
            return Collections.emptySet();
        }
        Set<String> values = new HashSet<>();
        if (value instanceof Iterable<?> iterable) {
            iterable.forEach(item -> values.add(String.valueOf(item)));
        } else {
            values.add(String.valueOf(value));
        }
        return Set.copyOf(values);
    }
}
