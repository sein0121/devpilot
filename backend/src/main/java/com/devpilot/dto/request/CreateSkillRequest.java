package com.devpilot.dto.request;

import com.devpilot.domain.SkillStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateSkillRequest(
        @NotBlank String name,
        Long categoryId, // nullable 허용
        @NotNull SkillStatus status,
        @Min(1) @Max(5) @NotNull Integer proficiency // 1부터 5까지의 정수, 빈 값 허용 X
) {
}