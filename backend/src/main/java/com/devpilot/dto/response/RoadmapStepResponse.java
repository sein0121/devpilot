package com.devpilot.dto.response;

import com.devpilot.domain.RoadmapStep;
import com.devpilot.domain.RoadmapStepStatus;

import java.time.LocalDate;

public record RoadmapStepResponse(
        Long id,
        String title,
        String description,
        RoadmapStepStatus status,
        LocalDate targetDate,
        String link,
        String skillName,
        Integer displayOrder
) {
    public static RoadmapStepResponse from(RoadmapStep step) {
        return new RoadmapStepResponse(
                step.getId(),
                step.getTitle(),
                step.getDescription(),
                step.getStatus(),
                step.getTargetDate(),   
                step.getLink(),
                step.getSkill() != null ? step.getSkill().getName() : null,
                step.getDisplayOrder()
        );
    }
}