package com.devpilot.dto.response;

public record RoadmapStepDraftResponse(
        String title,
        String description,
        String suggestedSkillName,
        Long matchedSkillId // 유저 Skill과 이름 매칭되면 채워짐, 안 되면 null
) {
}