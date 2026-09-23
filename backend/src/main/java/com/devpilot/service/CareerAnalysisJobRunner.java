package com.devpilot.service;

import com.devpilot.domain.CareerAnalysis;
import com.devpilot.repository.CareerAnalysisRepository;
import com.devpilot.service.ai.CareerAnalysisDataCollector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class CareerAnalysisJobRunner {

    private static final int ERROR_MESSAGE_MAX_LENGTH = 1000;

    private final CareerAnalysisWriter writer;
    private final CareerAnalysisRepository repository;
    private final CareerAnalysisDataCollector dataCollector;
    private final CareerAnalysisMetrics metrics;

    @Async("careerAnalysisExecutor")
    public void runAnalysis(Long analysisId, LocalDateTime requestedAt) {
        metrics.recordQueueWait(Duration.between(requestedAt, LocalDateTime.now()));
        metrics.decrementQueueSize();

        writer.markRunning(analysisId);
        Instant executionStart = Instant.now();
        try {
            String result = execute(analysisId);
            writer.markCompleted(analysisId, result);
            metrics.recordCompleted();
        } catch (Exception e) {
            log.error("Career analysis failed. analysisId={}", analysisId, e);
            writer.markFailed(analysisId, truncate(e.getMessage(), ERROR_MESSAGE_MAX_LENGTH));
            metrics.recordFailed();
        } finally {
            metrics.recordExecutionTime(Duration.between(executionStart, Instant.now()));
        }
    }

    String execute(Long analysisId) {
        CareerAnalysis analysis = repository.findById(analysisId)
                .orElseThrow(() -> new IllegalStateException("CareerAnalysis not found: " + analysisId));
        return dataCollector.collectAndAnalyze(analysis.getUserId());
    }

    private String truncate(String message, int max) {
        if (message == null) return null;
        return message.length() > max ? message.substring(0, max) : message;
    }
}