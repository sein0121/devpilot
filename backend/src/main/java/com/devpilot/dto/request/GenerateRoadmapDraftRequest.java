package com.devpilot.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record GenerateRoadmapDraftRequest(
        @NotBlank String goal,
        LocalDate targetDate
) {
}