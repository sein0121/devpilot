package com.devpilot.dto.github;

import java.util.Map;

public record GithubGraphQlRequest(String query, Map<String, Object> variables) {
}