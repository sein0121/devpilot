package com.devpilot.dto.response;

import com.devpilot.domain.Roadmap;
import com.devpilot.domain.RoadmapStep;
import com.devpilot.domain.RoadmapStepStatus;

import java.time.LocalDate;
import java.util.List;

public record RoadmapSummaryResponse(
        Long id,
        String title,
        LocalDate targetDate,
        int totalSteps,
        int doneSteps,
        int progress // 0~100
) {
    public static RoadmapSummaryResponse of(Roadmap roadmap, List<RoadmapStep> steps) {
        int total = steps.size();
        int done = (int) steps.stream().filter(s -> s.getStatus() == RoadmapStepStatus.DONE).count();
        int progress = total == 0 ? 0 : (done * 100) / total;

        return new RoadmapSummaryResponse(
                roadmap.getId(), roadmap.getTitle(), roadmap.getTargetDate(),
                total, done, progress
        );
    }
}