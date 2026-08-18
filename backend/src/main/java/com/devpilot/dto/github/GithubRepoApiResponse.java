package com.devpilot.dto.github;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GithubRepoApiResponse(
        String name,
        String description,
        String language,
        Integer stargazers_count,
        Boolean fork,
        String pushed_at
) {
}