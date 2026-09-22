package com.devpilot.service.ai;

// import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

@Component
// @ConditionalOnMissingBean(CareerAnalysisDataCollector.class)
class StubCareerAnalysisDataCollector implements CareerAnalysisDataCollector {

    @Override
    public String collectAndAnalyze(Long userId) {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return "{\"summary\":\"stub result for user " + userId + "\"}";
    }
}