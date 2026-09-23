package com.devpilot.global.exception;

import org.springframework.http.HttpStatus;

/**
 * GitHub API가 4xx(인증 문제 제외)를 반환한 경우 — 재시도해도 결과가 달라지지 않는 오류.
 */
public class GithubApiClientException extends BusinessException {
    public GithubApiClientException(String message) {
        super(HttpStatus.BAD_GATEWAY, message);
    }
}