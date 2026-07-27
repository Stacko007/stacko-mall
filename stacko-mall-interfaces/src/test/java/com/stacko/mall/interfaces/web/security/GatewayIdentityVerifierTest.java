package com.stacko.mall.interfaces.web.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GatewayIdentityVerifierTest {
    private static final String SECRET = "test-only-identity-secret-at-least-32-bytes";
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final GatewayIdentityVerifier verifier = new GatewayIdentityVerifier(objectMapper, properties());

    @Test
    void verifiesRequestBoundIdentity() {
        String identity = sign(Instant.now().getEpochSecond(), "GET", "/mall/api/admin/products");

        CurrentUser currentUser = verifier.verify(identity, "GET", "/mall/api/admin/products");

        assertEquals(3L, currentUser.getAccountId());
        assertEquals(7L, currentUser.getId());
        assertEquals("tenant-a", currentUser.getTenantId());
        assertEquals("stacko-mall-admin", currentUser.getAudience());
        assertEquals(java.util.Set.of("mall:product:list"), currentUser.getPermissions());
    }

    @Test
    void rejectsTamperedSignature() {
        String identity = sign(Instant.now().getEpochSecond(), "GET", "/mall/api/admin/products");

        assertThrows(GatewayIdentityException.class,
                () -> verifier.verify(identity + "x", "GET", "/mall/api/admin/products"));
    }

    @Test
    void rejectsReplayToDifferentMethodOrPath() {
        String identity = sign(Instant.now().getEpochSecond(), "GET", "/mall/api/admin/products");

        assertThrows(GatewayIdentityException.class,
                () -> verifier.verify(identity, "POST", "/mall/api/admin/products"));
        assertThrows(GatewayIdentityException.class,
                () -> verifier.verify(identity, "GET", "/mall/api/admin/orders"));
    }

    @Test
    void rejectsExpiredAndFutureIdentity() {
        String expired = sign(Instant.now().minusSeconds(31).getEpochSecond(),
                "GET", "/mall/api/admin/products");
        String future = sign(Instant.now().plusSeconds(6).getEpochSecond(),
                "GET", "/mall/api/admin/products");

        assertThrows(GatewayIdentityException.class,
                () -> verifier.verify(expired, "GET", "/mall/api/admin/products"));
        assertThrows(GatewayIdentityException.class,
                () -> verifier.verify(future, "GET", "/mall/api/admin/products"));
    }

    @Test
    void rejectsLegacyEnvelopeVersion() {
        String identity = sign(2, Instant.now().getEpochSecond(), "GET", "/mall/api/admin/products");

        assertThrows(GatewayIdentityException.class,
                () -> verifier.verify(identity, "GET", "/mall/api/admin/products"));
    }

    String sign(long issuedAt, String method, String path) {
        return sign(4, issuedAt, method, path, "stacko-mall-admin", "stacko-mall-admin");
    }

    String signForPortal(long issuedAt, String method, String path, String portalCode, String audience) {
        return sign(4, issuedAt, method, path, portalCode, audience);
    }

    private String sign(int version, long issuedAt, String method, String path) {
        return sign(version, issuedAt, method, path, "stacko-mall-admin", "stacko-mall-admin");
    }

    private String sign(int version, long issuedAt, String method, String path,
                        String portalCode, String audience) {
        try {
            Map<String, Object> envelope = new LinkedHashMap<>();
            envelope.put("version", version);
            envelope.put("accountId", "3");
            envelope.put("membershipId", "7");
            envelope.put("tenantId", "tenant-a");
            envelope.put("username", "alice");
            envelope.put("applicationCode", "stacko-mall");
            envelope.put("portalCode", portalCode);
            envelope.put("audience", audience);
            envelope.put("permissions", List.of("mall:product:list"));
            envelope.put("issuedAt", issuedAt);
            envelope.put("method", method);
            envelope.put("path", path);
            String payload = Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(objectMapper.writeValueAsBytes(envelope));
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            String signature = Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
            return payload + "." + signature;
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    static GatewayIdentityProperties properties() {
        GatewayIdentityProperties properties = new GatewayIdentityProperties();
        properties.setSigningSecret(SECRET);
        properties.setMaxAge(Duration.ofSeconds(30));
        properties.setAllowedClockSkew(Duration.ofSeconds(5));
        properties.setGatewayPathPrefix("/mall");
        properties.setProtectedPaths(java.util.List.of(
                "/api/admin/**",
                "/api/c/orders/**",
                "/api/c/after-sales/**"));
        return properties;
    }
}
