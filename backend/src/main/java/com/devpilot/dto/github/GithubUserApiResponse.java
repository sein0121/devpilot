package com.devpilot.dto.github;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GithubUserApiResponse(
        String login,
        Integer public_repos,
        Integer followers,
        Integer following
) {
}