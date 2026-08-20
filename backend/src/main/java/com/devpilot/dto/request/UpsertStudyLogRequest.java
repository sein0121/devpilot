package com.devpilot.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record UpsertStudyLogRequest(
        @NotBlank String title,
        @NotBlank String content,
        List<Long> skillIds // 없으면 빈 리스트로
) {
}