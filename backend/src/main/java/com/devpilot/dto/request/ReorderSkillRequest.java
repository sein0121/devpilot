package com.devpilot.dto.request;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record ReorderSkillRequest(
        @NotEmpty List<Long> orderedSkillIds // 유저가 드래그해서 정한 순서대로 id 나열
) {
}