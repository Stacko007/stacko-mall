package com.stacko.mall.interfaces.web.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class GatewayIdentityFilterTest {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final GatewayIdentityProperties properties = GatewayIdentityVerifierTest.properties();
    private final GatewayIdentityVerifier verifier = new GatewayIdentityVerifier(objectMapper, properties);
    private final CurrentUserContext currentUserContext = new CurrentUserContext();
    private final GatewayIdentityFilter filter = new GatewayIdentityFilter(
            verifier, currentUserContext, objectMapper, properties);
    private final GatewayIdentityVerifierTest signer = new GatewayIdentityVerifierTest();

    @AfterEach
    void resetRequestContext() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void protectedPathRequiresSignedIdentity() throws Exception {
        for (String path : java.util.List.of(
                "/api/admin/products",
                "/api/c/orders",
                "/api/c/after-sales/1")) {
            MockHttpServletRequest request = request("GET", path);
            MockHttpServletResponse response = new MockHttpServletResponse();

            filter.doFilter(request, response, unusedChain());

            assertEquals(401, response.getStatus());
        }
    }

    @Test
    void validIdentityBindsCurrentUser() throws Exception {
        MockHttpServletRequest request = request("GET", "/api/admin/products");
        request.addHeader("X-Tenant-ID", "tenant-a");
        request.addHeader(GatewayIdentityVerifier.IDENTITY_HEADER,
                signer.sign(Instant.now().getEpochSecond(), "GET", "/mall/api/admin/products"));
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<CurrentUser> forwarded = new AtomicReference<>();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        filter.doFilter(request, response,
                (servletRequest, servletResponse) ->
                        forwarded.set(currentUserContext.require("tenant-a")));

        assertEquals(200, response.getStatus());
        assertEquals(7L, forwarded.get().getId());
    }

    @Test
    void identityCannotBeReplayedToAnotherPath() throws Exception {
        MockHttpServletRequest request = request("GET", "/api/admin/orders");
        request.addHeader("X-Tenant-ID", "tenant-a");
        request.addHeader(GatewayIdentityVerifier.IDENTITY_HEADER,
                signer.sign(Instant.now().getEpochSecond(), "GET", "/mall/api/admin/products"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, unusedChain());

        assertEquals(401, response.getStatus());
    }

    @Test
    void tenantMismatchIsForbidden() throws Exception {
        MockHttpServletRequest request = request("GET", "/api/admin/products");
        request.addHeader("X-Tenant-ID", "tenant-b");
        request.addHeader(GatewayIdentityVerifier.IDENTITY_HEADER,
                signer.sign(Instant.now().getEpochSecond(), "GET", "/mall/api/admin/products"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, unusedChain());

        assertEquals(403, response.getStatus());
    }

    @Test
    void publicPathAllowsMissingIdentityButRejectsForgedIdentity() throws Exception {
        MockHttpServletRequest publicRequest = request("GET", "/api/c/products");
        MockHttpServletResponse publicResponse = new MockHttpServletResponse();
        AtomicReference<String> forwarded = new AtomicReference<>();

        filter.doFilter(publicRequest, publicResponse,
                (request, response) -> forwarded.set("yes"));

        assertEquals("yes", forwarded.get());

        MockHttpServletRequest forgedRequest = request("GET", "/api/c/products");
        forgedRequest.addHeader(GatewayIdentityVerifier.IDENTITY_HEADER, "forged");
        MockHttpServletResponse forgedResponse = new MockHttpServletResponse();
        filter.doFilter(forgedRequest, forgedResponse, unusedChain());

        assertEquals(401, forgedResponse.getStatus());
        assertNull(forgedRequest.getAttribute(CurrentUserContext.class.getName() + ".CURRENT_USER"));
    }

    private MockHttpServletRequest request(String method, String path) {
        MockHttpServletRequest request = new MockHttpServletRequest(method, path);
        request.setRequestURI(path);
        return request;
    }

    private FilterChain unusedChain() {
        return (request, response) -> {
        };
    }
}
