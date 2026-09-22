package com.devpilot.dto.response;

import com.devpilot.domain.CareerAnalysis;
import com.devpilot.domain.CareerAnalysisStatus;

import java.time.LocalDateTime;

public record CareerAnalysisResponse(
        Long id,
        CareerAnalysisStatus status,
        String result,
        String errorMessage,
        LocalDateTime startedAt,
        LocalDateTime completedAt
) {
    public static CareerAnalysisResponse from(CareerAnalysis analysis) {
        return new CareerAnalysisResponse(
                analysis.getId(),
                analysis.getStatus(),
                analysis.getResult(),
                analysis.getErrorMessage(),
                analysis.getStartedAt(),
                analysis.getCompletedAt()
        );
    }
}