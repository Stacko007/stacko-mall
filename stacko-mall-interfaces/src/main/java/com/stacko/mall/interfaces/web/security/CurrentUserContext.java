package com.stacko.mall.interfaces.web.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class CurrentUserContext {
    private static final String ATTRIBUTE = CurrentUserContext.class.getName() + ".CURRENT_USER";

    public CurrentUser require(String tenantId) {
        HttpServletRequest request = currentRequest();
        if (request != null) {
            Object cached = request.getAttribute(ATTRIBUTE);
            if (cached instanceof CurrentUser currentUser) {
                if (!tenantId.equals(currentUser.getTenantId())) {
                    throw new SecurityException("Tenant mismatch");
                }
                return currentUser;
            }
        }
        throw new SecurityException("Unauthorized");
    }

    public CurrentUser current() {
        HttpServletRequest request = currentRequest();
        if (request == null) {
            throw new SecurityException("No request context");
        }
        Object cached = request.getAttribute(ATTRIBUTE);
        if (cached instanceof CurrentUser currentUser) {
            return currentUser;
        }
        throw new SecurityException("Unauthorized");
    }

    void bind(HttpServletRequest request, CurrentUser currentUser) {
        request.setAttribute(ATTRIBUTE, currentUser);
    }

    private HttpServletRequest currentRequest() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            return attributes.getRequest();
        }
        return null;
    }
}
