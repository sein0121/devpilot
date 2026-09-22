package com.devpilot.service.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
class CareerGapAnalysisJsonSupport {

    private final ObjectMapper objectMapper;

    String toJson(CareerGapAnalysisResult result) {
        try {
            return objectMapper.writeValueAsString(result);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize CareerGapAnalysisResult", e);
        }
    }

    CareerGapAnalysisResult fromJson(String json) {
        try {
            return objectMapper.readValue(json, CareerGapAnalysisResult.class);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to deserialize CareerGapAnalysisResult", e);
        }
    }
}