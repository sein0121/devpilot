package com.devpilot.dto.response;

import com.devpilot.domain.Roadmap;
import com.devpilot.domain.RoadmapLink;
import com.devpilot.domain.RoadmapStep;
import com.devpilot.domain.RoadmapStepStatus;

import java.time.LocalDate;
import java.util.List;

public record RoadmapDetailResponse(
        Long id,
        String title,
        String description,
        LocalDate targetDate,
        int progress,
        List<RoadmapStepResponse> steps,
        List<RoadmapLinkResponse> links 
) {
    public static RoadmapDetailResponse of(
            Roadmap roadmap, List<RoadmapStep> steps, List<RoadmapLink> links
    ) {
        int total = steps.size();
        long done = steps.stream().filter(s -> s.getStatus() == RoadmapStepStatus.DONE).count();
        int progress = total == 0 ? 0 : (int) (done * 100 / total);

        return new RoadmapDetailResponse(
                roadmap.getId(), roadmap.getTitle(), roadmap.getDescription(), roadmap.getTargetDate(),
                progress,
                steps.stream().map(RoadmapStepResponse::from).toList(),
                links.stream().map(RoadmapLinkResponse::from).toList()
        );
    }
}