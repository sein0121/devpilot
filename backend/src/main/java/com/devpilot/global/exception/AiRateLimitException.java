package com.devpilot.global.exception;

import org.springframework.http.HttpStatus;

public class AiRateLimitException extends BusinessException {
    public AiRateLimitException() {
        super(HttpStatus.TOO_MANY_REQUESTS, "AI 요청이 많아 잠시 후 다시 시도해주세요.");
    }
}