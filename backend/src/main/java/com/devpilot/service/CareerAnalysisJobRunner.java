package com.devpilot.service;

import com.devpilot.domain.CareerAnalysis;
import com.devpilot.repository.CareerAnalysisRepository;
import com.devpilot.service.ai.CareerAnalysisDataCollector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CareerAnalysisJobRunner {

    private static final int ERROR_MESSAGE_MAX_LENGTH = 1000;

    private final CareerAnalysisWriter writer;
    private final CareerAnalysisRepository repository;
    private final CareerAnalysisDataCollector dataCollector;

    @Async("careerAnalysisExecutor")
    public void runAnalysis(Long analysisId) {
        writer.markRunning(analysisId);
        try {
            String result = execute(analysisId);
            writer.markCompleted(analysisId, result);
        } catch (Exception e) {
            log.error("Career analysis failed. analysisId={}", analysisId, e);
            writer.markFailed(analysisId, truncate(e.getMessage(), ERROR_MESSAGE_MAX_LENGTH));
        }
    }

    /** @Async 프록시를 우회해 동기 호출하기 위한 패키지 내부 진입점 (단위 테스트용). */
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