package com.devpilot.service.ai;

/**
 * Career Gap Analysis를 위한 AI 클라이언트.
 * AiRoadmapDraftClient와 책임이 다르므로(로드맵 초안 생성 vs 갭 분석) 별도로 분리한다.
 */
public interface AiCareerGapAnalysisClient {
    CareerGapAnalysisResult analyze(CareerAnalysisContext context);
}