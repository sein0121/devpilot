package com.devpilot.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record UpsertRoadmapRequest(
        @NotBlank String title,
        String description,
        LocalDate targetDate
) {
}