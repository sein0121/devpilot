package com.devpilot.dto.response;

import com.devpilot.domain.SkillCategory;

public record SkillCategoryResponse(
        Long id,
        String name,
        Long parentId
) {
    public static SkillCategoryResponse from(SkillCategory category) {
        return new SkillCategoryResponse(
                category.getId(),
                category.getName(),
                category.getParent() != null ? category.getParent().getId() : null
        );
    }
}