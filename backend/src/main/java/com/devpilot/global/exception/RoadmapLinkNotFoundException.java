package com.devpilot.global.exception;

import org.springframework.http.HttpStatus;

public class RoadmapLinkNotFoundException extends BusinessException {
    public RoadmapLinkNotFoundException(Long linkId) {
        super(HttpStatus.NOT_FOUND, "RoadmapLink not found: " + linkId);
    }
}