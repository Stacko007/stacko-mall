package com.stacko.mall.interfaces.web.security;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Validated
@ConfigurationProperties(prefix = "stacko.mall.gateway-identity")
public class GatewayIdentityProperties {
    @NotBlank
    @Size(min = 32)
    private String signingSecret;
    @NotNull
    private Duration maxAge = Duration.ofSeconds(30);
    @NotNull
    private Duration allowedClockSkew = Duration.ofSeconds(5);
    @NotBlank
    private String gatewayPathPrefix = "/mall";
    @NotBlank
    private String expectedApplicationCode = "stacko-mall";
    @NotBlank
    private String adminPortalCode = "stacko-mall-admin";
    @NotBlank
    private String adminAudience = "stacko-mall-admin";
    @NotBlank
    private String customerPortalCode = "stacko-mall-web";
    @NotBlank
    private String customerAudience = "stacko-mall-web";
    @Size(min = 1)
    private List<String> protectedPaths = new ArrayList<>();

    public String getSigningSecret() {
        return signingSecret;
    }

    public void setSigningSecret(String signingSecret) {
        this.signingSecret = signingSecret;
    }

    public Duration getMaxAge() {
        return maxAge;
    }

    public void setMaxAge(Duration maxAge) {
        this.maxAge = maxAge;
    }

    public Duration getAllowedClockSkew() {
        return allowedClockSkew;
    }

    public void setAllowedClockSkew(Duration allowedClockSkew) {
        this.allowedClockSkew = allowedClockSkew;
    }

    public String getGatewayPathPrefix() {
        return gatewayPathPrefix;
    }

    public void setGatewayPathPrefix(String gatewayPathPrefix) {
        this.gatewayPathPrefix = gatewayPathPrefix;
    }

    public List<String> getProtectedPaths() {
        return protectedPaths;
    }

    public void setProtectedPaths(List<String> protectedPaths) {
        this.protectedPaths = protectedPaths == null ? new ArrayList<>() : new ArrayList<>(protectedPaths);
    }

    public String getExpectedApplicationCode() {
        return expectedApplicationCode;
    }

    public void setExpectedApplicationCode(String expectedApplicationCode) {
        this.expectedApplicationCode = expectedApplicationCode;
    }

    public String getAdminPortalCode() {
        return adminPortalCode;
    }

    public void setAdminPortalCode(String adminPortalCode) {
        this.adminPortalCode = adminPortalCode;
    }

    public String getAdminAudience() {
        return adminAudience;
    }

    public void setAdminAudience(String adminAudience) {
        this.adminAudience = adminAudience;
    }

    public String getCustomerPortalCode() {
        return customerPortalCode;
    }

    public void setCustomerPortalCode(String customerPortalCode) {
        this.customerPortalCode = customerPortalCode;
    }

    public String getCustomerAudience() {
        return customerAudience;
    }

    public void setCustomerAudience(String customerAudience) {
        this.customerAudience = customerAudience;
    }
}
