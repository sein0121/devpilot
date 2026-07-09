package com.devpilot.global.exception;

import org.springframework.http.HttpStatus;

public class DuplicateEmailException extends BusinessException {
    public DuplicateEmailException(String email) {
        super(HttpStatus.BAD_REQUEST, "Email already exists: " + email);
    }
}
