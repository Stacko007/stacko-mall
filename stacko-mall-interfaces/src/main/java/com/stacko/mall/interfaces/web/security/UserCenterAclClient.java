package com.stacko.mall.interfaces.web.security;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class UserCenterAclClient {
    private final UserCenterProperties properties;
    private final RestClient restClient;

    public UserCenterAclClient(UserCenterProperties properties, RestClient.Builder restClientBuilder) {
        this.properties = properties;
        this.restClient = restClientBuilder
                .baseUrl(normalizeBaseUrl(properties.getBaseUrl()))
                .build();
    }

    public boolean hasPermission(String tenantId, String authorization, String permissionCode) {
        if (!properties.isEnabled()) {
            throw new SecurityException("User center permission check disabled");
        }
        if (tenantId == null || tenantId.isBlank()) {
            throw new SecurityException("Tenant required");
        }
        if (authorization == null || authorization.isBlank()) {
            throw new SecurityException("Unauthorized");
        }
        try {
            UserCenterApiResponse<Boolean> response = restClient.post()
                    .uri(properties.getAclCheckPath())
                    .header(HttpHeaders.AUTHORIZATION, authorization)
                    .header("X-Tenant-ID", tenantId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new PermissionCheckRequest(tenantId, permissionCode))
                    .retrieve()
                    .body(new ParameterizedTypeReference<UserCenterApiResponse<Boolean>>() {
                    });
            return response != null && response.isSuccess() && Boolean.TRUE.equals(response.getData());
        } catch (RestClientException ex) {
            throw new SecurityException("User center permission check failed", ex);
        }
    }

    private String normalizeBaseUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            return "http://localhost:8080";
        }
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    private record PermissionCheckRequest(String tenantId, String permissionCode) {
    }

    private static class UserCenterApiResponse<T> {
        private boolean success;
        private String message;
        private T data;

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public T getData() {
            return data;
        }

        public void setData(T data) {
            this.data = data;
        }
    }
}
