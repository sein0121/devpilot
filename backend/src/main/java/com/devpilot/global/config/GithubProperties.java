package com.devpilot.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

// @Value보다 @ConfigurationProperties를 쓴 이유는, 이제부터 GitHub 관련 설정(token, base-url 등)이 늘어날 가능성이 있어서 처음부터 묶어두는 게 낫다고 판단
@ConfigurationProperties(prefix = "github.api")
public record GithubProperties(String token) {
}