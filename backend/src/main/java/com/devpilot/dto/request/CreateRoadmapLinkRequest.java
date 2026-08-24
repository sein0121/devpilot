package com.devpilot.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateRoadmapLinkRequest(
        @NotBlank String url,
        String label
) {
}