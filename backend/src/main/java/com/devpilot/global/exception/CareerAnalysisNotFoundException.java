package com.devpilot.global.exception;

import org.springframework.http.HttpStatus;

public class CareerAnalysisNotFoundException extends BusinessException {
    public CareerAnalysisNotFoundException(Long id) {
        super(HttpStatus.NOT_FOUND, "CareerAnalysis not found: " + id);
    }
}