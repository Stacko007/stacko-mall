package com.stacko.mall.interfaces.web.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Set;

@Component
public class GatewayIdentityVerifier {
    public static final String IDENTITY_HEADER = "X-Stacko-Identity";
    private static final Base64.Decoder BASE64 = Base64.getUrlDecoder();

    private final ObjectMapper objectMapper;
    private final byte[] secret;
    private final long maxAgeSeconds;
    private final long allowedClockSkewSeconds;

    public GatewayIdentityVerifier(ObjectMapper objectMapper, GatewayIdentityProperties properties) {
        this.objectMapper = objectMapper;
        this.secret = properties.getSigningSecret().getBytes(StandardCharsets.UTF_8);
        this.maxAgeSeconds = requireRange(properties.getMaxAge().toSeconds(), 1, 60, "max age");
        this.allowedClockSkewSeconds = requireRange(
                properties.getAllowedClockSkew().toSeconds(), 0, 10, "allowed clock skew");
    }

    public CurrentUser verify(String signedIdentity, String method, String gatewayPath) {
        try {
            String[] parts = signedIdentity.split("\\.", -1);
            if (parts.length != 2 || parts[0].isBlank() || parts[1].isBlank()) {
                throw GatewayIdentityException.unauthorized();
            }
            byte[] expected = hmac(parts[0]);
            byte[] actual = BASE64.decode(parts[1]);
            if (!MessageDigest.isEqual(expected, actual)) {
                throw GatewayIdentityException.unauthorized();
            }
            IdentityEnvelope envelope = objectMapper.readValue(BASE64.decode(parts[0]), IdentityEnvelope.class);
            validate(envelope, method, gatewayPath);
            return new CurrentUser(
                    parseId(envelope.accountId()),
                    parseId(envelope.membershipId()),
                    envelope.username(),
                    envelope.tenantId(),
                    envelope.applicationCode(),
                    envelope.portalCode(),
                    envelope.audience(),
                    Set.copyOf(envelope.permissions()));
        } catch (GatewayIdentityException e) {
            throw e;
        } catch (Exception e) {
            throw GatewayIdentityException.unauthorized();
        }
    }

    private void validate(IdentityEnvelope envelope, String method, String gatewayPath) {
        if (envelope.version() != 4
                || !hasText(envelope.accountId())
                || !hasText(envelope.membershipId())
                || !hasText(envelope.tenantId())
                || !hasText(envelope.applicationCode())
                || !hasText(envelope.portalCode())
                || !hasText(envelope.audience())
                || envelope.permissions() == null
                || envelope.permissions().stream().anyMatch(permission -> !hasText(permission))
                || !method.equalsIgnoreCase(envelope.method())
                || !gatewayPath.equals(envelope.path())) {
            throw GatewayIdentityException.unauthorized();
        }
        long now = Instant.now().getEpochSecond();
        if (envelope.issuedAt() < now - maxAgeSeconds
                || envelope.issuedAt() > now + allowedClockSkewSeconds) {
            throw GatewayIdentityException.unauthorized();
        }
    }

    private byte[] hmac(String payload) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            return mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalStateException("Cannot verify Gateway identity", e);
        }
    }

    private Long parseId(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw GatewayIdentityException.unauthorized();
        }
    }

    private long requireRange(long value, long minimum, long maximum, String name) {
        if (value < minimum || value > maximum) {
            throw new IllegalStateException(
                    "Gateway identity " + name + " must be between " + minimum + " and " + maximum + " seconds");
        }
        return value;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    record IdentityEnvelope(
            int version,
            String accountId,
            String membershipId,
            String tenantId,
            String username,
            String applicationCode,
            String portalCode,
            String audience,
            List<String> permissions,
            long issuedAt,
            String method,
            String path
    ) {
    }
}
