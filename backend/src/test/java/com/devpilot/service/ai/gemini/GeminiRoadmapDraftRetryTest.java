package com.devpilot.service.ai.gemini;

import com.devpilot.global.exception.AiApiException;
import com.devpilot.global.exception.AiRateLimitException;
import com.devpilot.service.ai.AiRoadmapDraftClient;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.HttpServerErrorException;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.devpilot.support.AbstractIntegrationTest;              

class GeminiRoadmapDraftRetryTest extends AbstractIntegrationTest {

    static final MockWebServer server = new MockWebServer();

    static {
        try {
            server.start();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        // ← 기존 테스트가 쓰는 Gemini 주소 프로퍼티 키로 바꾸기
        registry.add("gemini.api.base-url", () -> server.url("/").toString());
        // 대기 시간만 줄여 테스트 속도 확보 (재시도 횟수·대상은 운영 설정 그대로)
        registry.add("resilience4j.retry.instances.geminiDraft.wait-duration", () -> "10ms");
    }

    @AfterAll
    static void shutdown() throws IOException {
        server.shutdown();
    }

    @Autowired
    AiRoadmapDraftClient client;

    @Autowired
    CircuitBreakerRegistry circuitBreakerRegistry;

    @BeforeEach
    void resetCircuitBreaker() {
        circuitBreakerRegistry.circuitBreaker("gemini").reset();
    }

    @Test
    void 서버_5xx는_한_번_재시도한다() {
        int before = server.getRequestCount();
        server.enqueue(new MockResponse().setResponseCode(503));
        server.enqueue(new MockResponse().setResponseCode(503));

        assertThatThrownBy(this::callDraft)
                .isInstanceOf(HttpServerErrorException.class);

        assertThat(server.getRequestCount() - before).isEqualTo(2);  // 최초 1 + 재시도 1
    }

    @Test
    void 클라이언트_4xx는_재시도하지_않는다() {
        int before = server.getRequestCount();
        server.enqueue(new MockResponse().setResponseCode(400));

        assertThatThrownBy(this::callDraft)
                .isInstanceOf(AiApiException.class);

        assertThat(server.getRequestCount() - before).isEqualTo(1);
    }

    @Test
    void rate_limit_429는_재시도하지_않는다() {
        int before = server.getRequestCount();
        server.enqueue(new MockResponse().setResponseCode(429));

        assertThatThrownBy(this::callDraft)
                .isInstanceOf(AiRateLimitException.class);

        assertThat(server.getRequestCount() - before).isEqualTo(1);
    }

    private void callDraft() {
        client.generateDraft("백엔드 개발자 취업", LocalDate.now().plusMonths(3), List.of("Java"));
    }
}