package com.devpilot.service.ai.gemini;

import com.devpilot.global.config.GeminiProperties;
import com.devpilot.global.exception.AiApiException;
import com.devpilot.global.exception.AiRateLimitException;
import com.devpilot.service.ai.AiRoadmapDraftClient;
import com.devpilot.service.ai.AiStepDraft;
import tools.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@ConditionalOnProperty(name = "ai.provider", havingValue = "gemini", matchIfMissing = true)
@RequiredArgsConstructor
public class GeminiRoadmapDraftClient implements AiRoadmapDraftClient {

    private final @Qualifier("geminiRestClient") RestClient geminiRestClient;
    private final GeminiProperties geminiProperties;
    private final ObjectMapper objectMapper;

    private static final Map<String, Object> STEP_RESPONSE_SCHEMA = Map.of(
            "type", "object",
            "properties", Map.of(
                    "steps", Map.of(
                            "type", "array",
                            "items", Map.of(
                                    "type", "object",
                                    "properties", Map.of(
                                            "title", Map.of("type", "string"),
                                            "description", Map.of("type", "string"),
                                            "skillName", Map.of("type", "string")
                                    ),
                                    "required", List.of("title", "skillName")
                            )
                    )
            ),
            "required", List.of("steps")
    );

    @Override
    public List<AiStepDraft> generateDraft(String goal, LocalDate targetDate, List<String> existingSkillNames) {
        String prompt = buildPrompt(goal, targetDate, existingSkillNames);

        GeminiRequest request = new GeminiRequest(
                List.of(new GeminiRequest.Content("user", List.of(new GeminiRequest.Part(prompt)))),
                new GeminiRequest.GenerationConfig("application/json", STEP_RESPONSE_SCHEMA)
        );

        GeminiResponse response = callWithRetry(request);

        String jsonText = extractText(response);
        GeminiStepsPayload payload = parsePayload(jsonText);

        return payload.steps().stream()
                .map(s -> new AiStepDraft(s.title(), s.description(), s.skillName()))
                .toList();
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
                log.error("Gemini 연결 실패 (attempt {})", attempt, e); // 추가
                lastError = new AiApiException("AI 서버 연결이 지연되고 있습니다.");
            } catch (Exception e) {
                log.error("Gemini 호출 실패 (attempt {})", attempt, e); // 추가
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
            Thread.sleep(1000); // 1초 대기 후 재시도
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

    private GeminiStepsPayload parsePayload(String jsonText) {
        try {
            return objectMapper.readValue(jsonText, GeminiStepsPayload.class);
        } catch (Exception e) {
            throw new AiApiException("AI 응답 형식을 해석하지 못했습니다.");
        }
    }

    private String buildPrompt(String goal, LocalDate targetDate, List<String> existingSkillNames) {
        String skillListText = existingSkillNames.isEmpty()
                ? "(등록된 스킬 없음)"
                : String.join(", ", existingSkillNames);

        return """
                당신은 개발자 커리어 코치입니다. 아래 목표를 달성하기 위한 학습 로드맵의 단계를 만들어주세요.

                목표: %s
                목표 기한: %s
                이 사람이 이미 가진 기술 목록: %s

                규칙:
                - 각 단계는 title(짧고 명확한 제목), description(1~2문장 설명), skillName(가장 관련있는 기술 하나)을 가져야 합니다.
                - skillName은 가능하면 위에 나열된 기존 기술 목록 중에서 고르되, 목표 달성에 꼭 필요한데 목록에 없는 기술이면 새로운 이름을 제안해도 됩니다.
                - 단계는 실행 가능한 순서로 정렬해주세요.
                - 반드시 한국어로 작성해주세요.
                """.formatted(
                goal,
                targetDate != null ? targetDate.toString() : "기한 없음",
                skillListText
        );
    }
}