package com.devpilot.global.exception;

import org.springframework.http.HttpStatus;

public class AiApiException extends BusinessException {
    public AiApiException(String message) {
        super(HttpStatus.BAD_GATEWAY, message);
    }
}