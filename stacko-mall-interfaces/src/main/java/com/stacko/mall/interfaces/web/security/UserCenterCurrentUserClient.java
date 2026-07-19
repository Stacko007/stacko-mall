package com.stacko.mall.interfaces.web.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class UserCenterCurrentUserClient {
    private final UserCenterProperties properties;
    private final RestClient restClient;

    public UserCenterCurrentUserClient(UserCenterProperties properties, RestClient.Builder restClientBuilder) {
        this.properties = properties;
        this.restClient = restClientBuilder
                .baseUrl(normalizeBaseUrl(properties.getBaseUrl()))
                .build();
    }

    public CurrentUser currentUser(String tenantId, String authorization) {
        if (!properties.isEnabled()) {
            throw new SecurityException("User center current user check disabled");
        }
        if (tenantId == null || tenantId.isBlank()) {
            throw new SecurityException("Tenant required");
        }
        if (authorization == null || authorization.isBlank()) {
            throw new SecurityException("Unauthorized");
        }
        try {
            UserCenterApiResponse<CurrentUser> response = restClient.get()
                    .uri(properties.getCurrentUserPath())
                    .header(HttpHeaders.AUTHORIZATION, authorization)
                    .header("X-Tenant-ID", tenantId)
                    .retrieve()
                    .body(new ParameterizedTypeReference<UserCenterApiResponse<CurrentUser>>() {
                    });
            if (response == null || !response.isSuccess() || response.getData() == null) {
                throw new SecurityException(response == null ? "Current user missing" : response.getMessage());
            }
            CurrentUser currentUser = response.getData();
            if (!tenantId.equals(currentUser.getTenantId())) {
                throw new SecurityException("Tenant mismatch");
            }
            return currentUser;
        } catch (RestClientException ex) {
            throw new SecurityException("User center current user check failed", ex);
        }
    }

    private String normalizeBaseUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            return "http://localhost:8080";
        }
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    @Getter
    @Setter
    private static class UserCenterApiResponse<T> {
        private boolean success;
        private String message;
        private T data;
    }
}
