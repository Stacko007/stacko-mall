package com.stacko.mall.interfaces.web.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.server.PathContainer;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class GatewayIdentityFilter extends OncePerRequestFilter {
    private static final String TENANT_HEADER = "X-Tenant-ID";
    private static final String TRACE_HEADER = "X-Request-Id";

    private final GatewayIdentityVerifier verifier;
    private final CurrentUserContext currentUserContext;
    private final ObjectMapper objectMapper;
    private final String gatewayPathPrefix;
    private final String expectedApplicationCode;
    private final String adminPortalCode;
    private final String adminAudience;
    private final String customerPortalCode;
    private final String customerAudience;
    private final List<PathPattern> protectedPaths;

    public GatewayIdentityFilter(GatewayIdentityVerifier verifier,
                                 CurrentUserContext currentUserContext,
                                 ObjectMapper objectMapper,
                                 GatewayIdentityProperties properties) {
        this.verifier = verifier;
        this.currentUserContext = currentUserContext;
        this.objectMapper = objectMapper;
        this.gatewayPathPrefix = normalizePrefix(properties.getGatewayPathPrefix());
        this.expectedApplicationCode = properties.getExpectedApplicationCode();
        this.adminPortalCode = properties.getAdminPortalCode();
        this.adminAudience = properties.getAdminAudience();
        this.customerPortalCode = properties.getCustomerPortalCode();
        this.customerAudience = properties.getCustomerAudience();
        PathPatternParser parser = new PathPatternParser();
        this.protectedPaths = properties.getProtectedPaths().stream().map(parser::parse).toList();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String signedIdentity = request.getHeader(GatewayIdentityVerifier.IDENTITY_HEADER);
        if (signedIdentity == null || signedIdentity.isBlank()) {
            if (isProtected(request.getRequestURI())) {
                writeError(response, GatewayIdentityException.unauthorized());
                return;
            }
            filterChain.doFilter(request, response);
            return;
        }

        try {
            CurrentUser currentUser = verifier.verify(
                    signedIdentity,
                    request.getMethod(),
                    gatewayPathPrefix + request.getRequestURI());
            String tenantId = request.getHeader(TENANT_HEADER);
            if (tenantId == null || !tenantId.equals(currentUser.getTenantId())) {
                throw GatewayIdentityException.forbidden();
            }
            validatePortal(request.getRequestURI(), currentUser);
            currentUserContext.bind(request, currentUser);
            filterChain.doFilter(request, response);
        } catch (GatewayIdentityException e) {
            writeError(response, e);
        }
    }

    private boolean isProtected(String path) {
        PathContainer container = PathContainer.parsePath(path);
        return protectedPaths.stream().anyMatch(pattern -> pattern.matches(container));
    }

    private void validatePortal(String path, CurrentUser currentUser) {
        if (!expectedApplicationCode.equals(currentUser.getApplicationCode())) {
            throw GatewayIdentityException.forbidden();
        }
        if (path.equals("/api/admin") || path.startsWith("/api/admin/")) {
            requirePortal(currentUser, adminPortalCode, adminAudience);
            return;
        }
        if (path.equals("/api/c") || path.startsWith("/api/c/")) {
            requirePortal(currentUser, customerPortalCode, customerAudience);
        }
    }

    private void requirePortal(CurrentUser currentUser, String portalCode, String audience) {
        if (!portalCode.equals(currentUser.getPortalCode()) || !audience.equals(currentUser.getAudience())) {
            throw GatewayIdentityException.forbidden();
        }
    }

    private String normalizePrefix(String prefix) {
        String normalized = prefix.trim();
        if (!normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }
        while (normalized.length() > 1 && normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    private void writeError(HttpServletResponse response, GatewayIdentityException error) throws IOException {
        response.setStatus(error.getStatus());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", false);
        body.put("message", error.getMessage());
        body.put("data", null);
        body.put("traceId", response.getHeader(TRACE_HEADER));
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
