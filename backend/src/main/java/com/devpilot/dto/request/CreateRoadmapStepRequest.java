package com.devpilot.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateRoadmapStepRequest(
        @NotNull Long skillId, // 생성 시엔 필수
        @NotBlank String title,
        String description,
        LocalDate targetDate
) {
}