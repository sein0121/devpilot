package com.devpilot.global.exception;

import org.springframework.http.HttpStatus;

public class GithubAccountRequiredException extends BusinessException {
    public GithubAccountRequiredException() {
        super(HttpStatus.PRECONDITION_REQUIRED,
                "Career Gap Analysis를 위해 먼저 GitHub 계정을 연동해주세요.");
    }
}