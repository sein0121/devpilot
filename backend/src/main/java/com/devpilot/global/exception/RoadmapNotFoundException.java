package com.devpilot.global.exception;

import org.springframework.http.HttpStatus;

public class RoadmapNotFoundException extends BusinessException {
    public RoadmapNotFoundException(Long roadmapId) {
        super(HttpStatus.NOT_FOUND, "Roadmap not found: " + roadmapId);
    }
}