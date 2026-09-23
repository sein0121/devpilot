package com.devpilot.support;

import com.devpilot.service.ai.AiCareerGapAnalysisClient;
import com.devpilot.service.ai.CareerAnalysisContext;
import com.devpilot.service.ai.CareerGapAnalysisResult;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.util.List;

@TestConfiguration(proxyBeanMethods = false)
public class TestAiConfig {

    @Bean
    @Primary
    public AiCareerGapAnalysisClient testAiCareerGapAnalysisClient() {
        return context -> new CareerGapAnalysisResult(
                "test summary",
                List.of(),
                List.of(),
                List.of()
        );
    }
}