package com.devpilot.dto.request;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record ReorderStepsRequest(
        @NotEmpty List<Long> orderedStepIds
) {
}