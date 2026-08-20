package com.devpilot.global.exception;

import org.springframework.http.HttpStatus;

public class RoadmapStepNotFoundException extends BusinessException {
    public RoadmapStepNotFoundException(Long stepId) {
        super(HttpStatus.NOT_FOUND, "RoadmapStep not found: " + stepId);
    }
}