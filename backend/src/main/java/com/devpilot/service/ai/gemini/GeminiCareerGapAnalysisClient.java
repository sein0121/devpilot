package com.devpilot.service.ai.gemini;

import com.devpilot.global.config.GeminiProperties;
import com.devpilot.global.exception.AiApiException;
import com.devpilot.global.exception.AiRateLimitException;
import com.devpilot.service.ai.AiCareerGapAnalysisClient;
import com.devpilot.service.ai.CareerAnalysisContext;
import com.devpilot.service.ai.CareerGapAnalysisResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@ConditionalOnProperty(name = "ai.provider", havingValue = "gemini", matchIfMissing = true)
@RequiredArgsConstructor
public class GeminiCareerGapAnalysisClient implements AiCareerGapAnalysisClient {

    private final @Qualifier("geminiRestClient") RestClient geminiRestClient;
    private final GeminiProperties geminiProperties;
    private final ObjectMapper objectMapper;

    private static final Map<String, Object> RESULT_RESPONSE_SCHEMA = Map.of(
            "type", "object",
            "properties", Map.of(
                    "summary", Map.of("type", "string"),
                    "strengths", Map.of(
                            "type", "array",
                            "items", Map.of(
                                    "type", "object",
                                    "properties", Map.of(
                                            "title", Map.of("type", "string"),
                                            "evidence", Map.of("type", "string")
                                    ),
                                    "required", List.of("title", "evidence")
                            )
                    ),
                    "gaps", Map.of(
                            "type", "array",
                            "items", Map.of(
                                    "type", "object",
                                    "properties", Map.of(
                                            "skill", Map.of("type", "string"),
                                            "reason", Map.of("type", "string"),
                                            "priority", Map.of("type", "string", "enum", List.of("HIGH", "MEDIUM", "LOW"))
                                    ),
                                    "required", List.of("skill", "reason", "priority")
                            )
                    ),
                    "recommendations", Map.of(
                            "type", "array",
                            "items", Map.of(
                                    "type", "object",
                                    "properties", Map.of(
                                            "title", Map.of("type", "string"),
                                            "description", Map.of("type", "string"),
                                            "relatedSkill", Map.of("type", "string"),
                                            "priority", Map.of("type", "string", "enum", List.of("HIGH", "MEDIUM", "LOW"))
                                    ),
                                    "required", List.of("title", "description", "relatedSkill", "priority")
                            )
                    )
            ),
            "required", List.of("summary", "strengths", "gaps", "recommendations")
    );

    @Override
    public CareerGapAnalysisResult analyze(CareerAnalysisContext context) {
        String prompt = buildPrompt(context);

        GeminiRequest request = new GeminiRequest(
                List.of(new GeminiRequest.Content("user", List.of(new GeminiRequest.Part(prompt)))),
                new GeminiRequest.GenerationConfig("application/json", RESULT_RESPONSE_SCHEMA)
        );

        GeminiResponse response = callWithRetry(request);
        String jsonText = extractText(response);
        return parseResult(jsonText);
    }

    private GeminiResponse callWithRetry(GeminiRequest request) {
        int maxAttempts = 2;
        RuntimeException lastError = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return geminiRestClient.post()
                        .uri("/models/{model}:generateContent", geminiProperties.model())
                        .body(request)
                        .retrieve()
                        .body(GeminiResponse.class);
            } catch (HttpClientErrorException.TooManyRequests e) {
                throw new AiRateLimitException();
            } catch (ResourceAccessException e) {
                log.error("Gemini 연결 실패 (attempt {})", attempt, e);
                lastError = new AiApiException("AI 서버 연결이 지연되고 있습니다.");
            } catch (Exception e) {
                log.error("Gemini 호출 실패 (attempt {})", attempt, e);
                lastError = new AiApiException("AI 응답을 받아오는 데 실패했습니다.");
            }

            if (attempt < maxAttempts) {
                sleepBeforeRetry();
            }
        }

        throw lastError;
    }

    private void sleepBeforeRetry() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private String extractText(GeminiResponse response) {
        if (response == null || response.candidates() == null || response.candidates().isEmpty()) {
            throw new AiApiException("AI로부터 빈 응답을 받았습니다.");
        }
        return response.candidates().get(0).content().parts().get(0).text();
    }

    private CareerGapAnalysisResult parseResult(String jsonText) {
        try {
            return objectMapper.readValue(jsonText, CareerGapAnalysisResult.class);
        } catch (Exception e) {
            throw new AiApiException("AI 응답 형식을 해석하지 못했습니다.");
        }
    }

    private String buildPrompt(CareerAnalysisContext context) {
        return """
                당신은 개발자 커리어 코치입니다. 아래 데이터를 분석해 이 개발자의 강점, 부족한 부분(갭),
                그리고 다음에 무엇을 하면 좋을지 추천해주세요.

                분석 기간: %s ~ %s

                [GitHub 활동]
                %s

                [최근 학습 기록]
                %s

                [보유 기술]
                %s

                [진행 중인 로드맵]
                %s

                규칙:
                - strengths는 데이터에 실제로 근거가 있는 것만 작성하고, evidence에 구체적인 수치나 사실을 인용하세요.
                - gaps는 등록된 기술/학습 기록/로드맵에서 부족하거나 누락된 부분을 근거로 제시하세요.
                - recommendations는 gaps를 해결하기 위한 구체적이고 실행 가능한 다음 행동이어야 합니다.
                - strengths, gaps, recommendations는 각각 최대 5개까지만 작성하세요.
                - priority는 HIGH, MEDIUM, LOW 중 하나로만 작성하세요.
                - 반드시 한국어로 작성해주세요.
                """.formatted(
                context.periodStart(), context.periodEnd(),
                formatGithub(context.github()),
                formatStudyLogs(context.studyLogs()),
                formatSkills(context.skills()),
                formatRoadmaps(context.roadmaps())
        );
    }

    private String formatGithub(CareerAnalysisContext.GithubSummary github) {
        if (github.githubUsername() == null) {
            return "(GitHub 연동 없음)";
        }
        String languages = github.languageUsage().isEmpty()
                ? "(언어 정보 없음)"
                : github.languageUsage().stream()
                        .map(l -> l.language() + " " + l.repositoryCount() + "개")
                        .collect(Collectors.joining(", "));

        String repos = github.notableRepositories().isEmpty()
                ? "(레포지토리 없음)"
                : github.notableRepositories().stream()
                        .map(r -> "- " + r.name() + " (" + (r.language() != null ? r.language() : "언어 미상")
                                + ", star " + r.stars() + (r.isFork() ? ", fork" : "") + ")")
                        .collect(Collectors.joining("\n"));

        return """
                username: %s
                기간 내 총 커밋 수: %d, 활동일 수: %d
                언어 분포: %s
                주요 레포지토리:
                %s
                """.formatted(
                github.githubUsername(),
                github.totalContributionsInPeriod(),
                github.activeDaysInPeriod(),
                languages,
                repos
        );
    }

    private String formatStudyLogs(List<CareerAnalysisContext.StudyLogSummary> studyLogs) {
        if (studyLogs.isEmpty()) {
            return "(학습 기록 없음)";
        }
        return studyLogs.stream()
                .map(log -> "- [" + log.logDate() + "] " + log.title()
                        + (log.relatedSkillNames().isEmpty() ? "" : " (관련 기술: " + String.join(", ", log.relatedSkillNames()) + ")"))
                .collect(Collectors.joining("\n"));
    }

    private String formatSkills(List<CareerAnalysisContext.SkillSummary> skills) {
        if (skills.isEmpty()) {
            return "(등록된 기술 없음)";
        }
        return skills.stream()
                .map(s -> "- " + s.name() + (s.categoryName() != null ? " [" + s.categoryName() + "]" : "")
                        + " (" + s.status() + ", 숙련도 " + s.proficiency() + "/5)")
                .collect(Collectors.joining("\n"));
    }

    private String formatRoadmaps(List<CareerAnalysisContext.RoadmapSummary> roadmaps) {
        if (roadmaps.isEmpty()) {
            return "(등록된 로드맵 없음)";
        }
        return roadmaps.stream()
                .map(r -> "- " + r.roadmapTitle() + "\n" +
                        r.steps().stream()
                                .map(s -> "  · " + s.title() + " (" + s.status()
                                        + (s.relatedSkillName() != null ? ", " + s.relatedSkillName() : "") + ")")
                                .collect(Collectors.joining("\n")))
                .collect(Collectors.joining("\n"));
    }
}