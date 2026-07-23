package com.stacko.mall.interfaces.web.security;

public class GatewayIdentityException extends SecurityException {
    private final int status;

    private GatewayIdentityException(int status, String message) {
        super(message);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    public static GatewayIdentityException unauthorized() {
        return new GatewayIdentityException(401, "Unauthorized");
    }

    public static GatewayIdentityException forbidden() {
        return new GatewayIdentityException(403, "Forbidden");
    }
}
