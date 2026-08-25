// AI한테 로드맵 초안을 뽑아달라고 요청하는 기능
package com.devpilot.service.ai;

import java.time.LocalDate;
import java.util.List;

public interface AiRoadmapDraftClient {
    List<AiStepDraft> generateDraft(String goal, LocalDate targetDate, List<String> existingSkillNames);
}