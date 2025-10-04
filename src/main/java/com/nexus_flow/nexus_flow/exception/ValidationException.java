package com.nexus_flow.nexus_flow.exception;

public class ValidationException extends BusinessException {
    public ValidationException(String message) {
        super(message, "VALIDATION_ERROR");
    }
}