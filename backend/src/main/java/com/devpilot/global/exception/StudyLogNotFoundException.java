package com.devpilot.global.exception;

import org.springframework.http.HttpStatus;
import java.time.LocalDate;

public class StudyLogNotFoundException extends BusinessException {
    public StudyLogNotFoundException(LocalDate date) {
        super(HttpStatus.NOT_FOUND, "StudyLog not found for date: " + date);
    }
}