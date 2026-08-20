package com.devpilot.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record UpdateRoadmapStepRequest(
        Long skillId, // 수정 시엔 선택 (null이면 연결 해제)
        @NotBlank String title,
        String description,
        LocalDate targetDate
) {
}