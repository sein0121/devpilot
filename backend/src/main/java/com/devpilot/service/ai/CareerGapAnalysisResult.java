package com.devpilot.service.ai;

import java.util.List;

/**
 * Career Gap Analysis의 최종 결과.
 * CareerAnalysis.result 컬럼에 JSON 문자열로 직렬화되어 저장된다.
 */
public record CareerGapAnalysisResult(
        String summary,
        List<Strength> strengths,
        List<Gap> gaps,
        List<Recommendation> recommendations
) {

    public record Strength(
            String title,
            String evidence
    ) {}

    public record Gap(
            String skill,
            String reason,
            Priority priority
    ) {}

    public record Recommendation(
            String title,
            String description,
            String relatedSkill,
            Priority priority
    ) {}

    public enum Priority {
        HIGH, MEDIUM, LOW
    }
}