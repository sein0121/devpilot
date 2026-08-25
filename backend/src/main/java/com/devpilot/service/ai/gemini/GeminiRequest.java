package com.devpilot.service.ai.gemini;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Map;

public record GeminiRequest(
        List<Content> contents,
        @JsonInclude(JsonInclude.Include.NON_NULL) GenerationConfig generationConfig
) {
    public record Content(String role, List<Part> parts) {}
    public record Part(String text) {}
    public record GenerationConfig(String responseMimeType, Map<String, Object> responseSchema) {}
}