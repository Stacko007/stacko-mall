package com.stacko.mall.interfaces.web.security;

public class AuthenticationServiceUnavailableException extends RuntimeException {
    public AuthenticationServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
