package com.devpilot.dto.request;

import com.devpilot.domain.SkillStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateSkillRequest(
        @NotBlank String name,
        Long categoryId,
        @NotNull SkillStatus status,
        @Min(1) @Max(5) @NotNull Integer proficiency
) {
}