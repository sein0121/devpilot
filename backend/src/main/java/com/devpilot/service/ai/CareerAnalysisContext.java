package com.devpilot.service.ai;

import com.devpilot.domain.RoadmapStepStatus;
import com.devpilot.domain.SkillStatus;

import java.time.LocalDate;
import java.util.List;

/**
 * Career Gap Analysis에 필요한 입력 데이터.
 * GitHub / StudyLog / Skill / Roadmap 각 도메인에서 조회한 데이터를
 * AI가 판단하기 좋은 형태로 요약해 담는다.
 */
public record CareerAnalysisContext(
        Long userId,
        LocalDate periodStart,
        LocalDate periodEnd,
        GithubSummary github,
        List<StudyLogSummary> studyLogs,
        List<SkillSummary> skills,
        List<RoadmapSummary> roadmaps
) {

    public record GithubSummary(
            String githubUsername,
            int totalContributionsInPeriod,
            int activeDaysInPeriod,
            List<LanguageUsage> languageUsage,
            List<RepositorySummary> notableRepositories
    ) {
        public record LanguageUsage(String language, int repositoryCount) {}

        public record RepositorySummary(
                String name,
                String language,
                int stars,
                boolean isFork,
                LocalDate pushedAt
        ) {}
    }

    public record StudyLogSummary(
            LocalDate logDate,
            String title,
            List<String> relatedSkillNames
    ) {}

    public record SkillSummary(
            String name,
            String categoryName,
            SkillStatus status,
            int proficiency
    ) {}

    public record RoadmapSummary(
            String roadmapTitle,
            List<RoadmapStepSummary> steps
    ) {
        public record RoadmapStepSummary(
                String title,
                RoadmapStepStatus status,
                String relatedSkillName
        ) {}
    }
}