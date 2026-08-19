package com.devpilot.global.exception;

import org.springframework.http.HttpStatus;

public class GithubApiAuthException extends BusinessException {
    public GithubApiAuthException() {
        super(HttpStatus.BAD_GATEWAY, "GitHub API 인증에 실패했습니다. PAT 토큰이 만료되었거나 유효하지 않습니다.");
    }
}