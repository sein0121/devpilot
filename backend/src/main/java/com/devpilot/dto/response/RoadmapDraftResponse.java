package com.devpilot.dto.response;

import java.util.List;

public record RoadmapDraftResponse(
        List<RoadmapStepDraftResponse> steps
) {
}