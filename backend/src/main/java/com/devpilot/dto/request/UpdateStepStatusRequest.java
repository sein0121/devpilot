package com.devpilot.dto.request;

import com.devpilot.domain.RoadmapStepStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateStepStatusRequest(
        @NotNull RoadmapStepStatus status
) {
}