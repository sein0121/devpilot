package com.devpilot.service.ai;

import com.devpilot.domain.*;
import com.devpilot.global.config.CareerAnalysisProperties;
import com.devpilot.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
class RealCareerAnalysisDataCollector implements CareerAnalysisDataCollector {

    private static final int NOTABLE_REPOSITORY_LIMIT = 10;

    private final CareerAnalysisProperties properties;
    private final UserRepository userRepository;
    private final GithubAccountRepository githubAccountRepository;
    private final GithubContributionRepository githubContributionRepository;
    private final GithubRepositoryRepository githubRepositoryRepository;
    private final StudyLogRepository studyLogRepository;
    private final SkillRepository skillRepository;
    private final RoadmapRepository roadmapRepository;
    private final RoadmapStepRepository roadmapStepRepository;
    private final AiCareerGapAnalysisClient aiClient;
    private final CareerGapAnalysisJsonSupport jsonSupport;

    @Override
    public String collectAndAnalyze(Long userId) {
        CareerAnalysisContext context = buildContext(userId);
        CareerGapAnalysisResult result = aiClient.analyze(context);
        return jsonSupport.toJson(result); // static 호출 → 인스턴스 메서드 호출로 변경
    }

    private CareerAnalysisContext buildContext(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found: " + userId));

        LocalDate periodEnd = LocalDate.now();
        LocalDate periodStart = periodEnd.minusDays(properties.lookbackDays());

        return new CareerAnalysisContext(
                userId,
                periodStart,
                periodEnd,
                buildGithubSummary(user, periodStart, periodEnd),
                buildStudyLogSummaries(user, periodStart, periodEnd),
                buildSkillSummaries(user),
                buildRoadmapSummaries(user)
        );
    }

    private CareerAnalysisContext.GithubSummary buildGithubSummary(
        User user, LocalDate periodStart, LocalDate periodEnd
    ) {
        GithubAccount account = githubAccountRepository.findByUser(user)
                .orElseThrow(() -> new IllegalStateException(
                        "GithubAccount not found despite prior validation. userId=" + user.getId()));

        List<GithubContribution> contributions = githubContributionRepository
                .findByGithubAccountAndDateBetween(account, periodStart, periodEnd);

        int totalContributions = contributions.stream().mapToInt(GithubContribution::getCount).sum();
        int activeDays = (int) contributions.stream().filter(c -> c.getCount() > 0).count();

        List<GithubRepository> repositories = githubRepositoryRepository
                .findByGithubAccountOrderByPushedAtDesc(account); // 이미 pushedAt DESC 정렬됨

        List<CareerAnalysisContext.GithubSummary.LanguageUsage> languageUsage = repositories.stream()
                .filter(r -> r.getLanguage() != null)
                .collect(Collectors.groupingBy(GithubRepository::getLanguage, Collectors.counting()))
                .entrySet().stream()
                .map(e -> new CareerAnalysisContext.GithubSummary.LanguageUsage(e.getKey(), e.getValue().intValue()))
                .sorted(Comparator.comparingInt(CareerAnalysisContext.GithubSummary.LanguageUsage::repositoryCount).reversed())
                .toList();

        List<CareerAnalysisContext.GithubSummary.RepositorySummary> notableRepositories = repositories.stream()
                .limit(NOTABLE_REPOSITORY_LIMIT) // 이미 pushedAt DESC로 정렬되어 있으니 별도 정렬 불필요
                .map(r -> new CareerAnalysisContext.GithubSummary.RepositorySummary(
                        r.getName(),
                        r.getLanguage(),
                        r.getStars() != null ? r.getStars() : 0,
                        Boolean.TRUE.equals(r.getIsFork()),
                        r.getPushedAt() != null ? r.getPushedAt().atZone(ZoneId.systemDefault()).toLocalDate() : null
                ))
                .toList();

        return new CareerAnalysisContext.GithubSummary(
                account.getGithubUsername(), totalContributions, activeDays, languageUsage, notableRepositories
        );
    }

    private List<CareerAnalysisContext.StudyLogSummary> buildStudyLogSummaries(
        User user, LocalDate periodStart, LocalDate periodEnd
    ) {
        return studyLogRepository.findByUserAndDateBetweenOrderByDateDesc(user, periodStart, periodEnd).stream()
                .map(log -> new CareerAnalysisContext.StudyLogSummary(
                        log.getDate(),
                        log.getTitle(),
                        log.getSkills().stream().map(Skill::getName).toList()
                ))
                .toList();
    }

    private List<CareerAnalysisContext.SkillSummary> buildSkillSummaries(User user) {
        return skillRepository.findByUserOrderByDisplayOrderAsc(user).stream()
                .map(skill -> new CareerAnalysisContext.SkillSummary(
                        skill.getName(),
                        skill.getCategory() != null ? skill.getCategory().getName() : null,
                        skill.getStatus(),
                        skill.getProficiency()
                ))
                .toList();
    }

    private List<CareerAnalysisContext.RoadmapSummary> buildRoadmapSummaries(User user) {
        return roadmapRepository.findByUserOrderByIdDesc(user).stream()
                .map(roadmap -> {
                    List<CareerAnalysisContext.RoadmapSummary.RoadmapStepSummary> steps =
                            roadmapStepRepository.findByRoadmapOrderByDisplayOrderAsc(roadmap).stream()
                                    .map(step -> new CareerAnalysisContext.RoadmapSummary.RoadmapStepSummary(
                                            step.getTitle(),
                                            step.getStatus(),
                                            step.getSkill() != null ? step.getSkill().getName() : null
                                    ))
                                    .toList();
                    return new CareerAnalysisContext.RoadmapSummary(roadmap.getTitle(), steps);
                })
                .toList();
    }
}