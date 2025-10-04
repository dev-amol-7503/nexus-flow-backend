package com.nexus_flow.nexus_flow.exception;

public class AuthenticationException extends BusinessException {
    public AuthenticationException(String message) {
        super(message, "AUTHENTICATION_ERROR");
    }
}