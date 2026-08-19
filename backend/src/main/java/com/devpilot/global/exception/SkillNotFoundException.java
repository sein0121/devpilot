package com.devpilot.global.exception;

import org.springframework.http.HttpStatus;

public class SkillNotFoundException extends BusinessException {
    public SkillNotFoundException(Long skillId) {
        super(HttpStatus.NOT_FOUND, "Skill not found: " + skillId);
    }
}