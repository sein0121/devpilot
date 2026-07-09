package com.devpilot.global.exception;

import org.springframework.http.HttpStatus;

public class UserNotFoundException extends BusinessException {
    public UserNotFoundException(Long id) {
        super(HttpStatus.NOT_FOUND, "User not found. id : " + id);
    }
}
