package com.devpilot.service.ai.gemini;

import java.util.List;

// Gemini가 실제로 채워 넣는 JSON 본문 (Part.text 안에 문자열로 들어있음)
public record GeminiStepsPayload(List<StepDraft> steps) {
    public record StepDraft(String title, String description, String skillName) {}
}