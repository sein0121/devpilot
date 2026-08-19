package com.devpilot.dto.response;

import com.devpilot.domain.Skill;
import com.devpilot.domain.SkillStatus;

public record SkillResponse(
        Long id,
        String name,
        String categoryName,
        SkillStatus status,
        Integer proficiency,
        Integer displayOrder
) {
    public static SkillResponse from(Skill skill) {
        return new SkillResponse(
                skill.getId(),
                skill.getName(),
                skill.getCategory() != null ? skill.getCategory().getName() : null,
                skill.getStatus(),
                skill.getProficiency(),
                skill.getDisplayOrder()
        );
    }
}