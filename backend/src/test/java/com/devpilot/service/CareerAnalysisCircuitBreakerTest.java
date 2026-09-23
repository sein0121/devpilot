package com.devpilot.service;

import com.devpilot.domain.GithubAccount;
import com.devpilot.domain.User;
import com.devpilot.repository.CareerAnalysisRepository;
import com.devpilot.repository.GithubAccountRepository;
import com.devpilot.repository.UserRepository;
import com.devpilot.support.AbstractIntegrationTest;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@TestPropertySource(properties = "gemini.api.base-url=http://localhost:1")
class CareerAnalysisCircuitBreakerTest extends AbstractIntegrationTest {

    @Autowired
    private CareerAnalysisService careerAnalysisService;

    @Autowired
    private CareerAnalysisRepository careerAnalysisRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GithubAccountRepository githubAccountRepository;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    private Long userId;

    @BeforeEach
    void setUp() {
        User user = User.createGithubUser("circuit-test@devpilot.com", "circuit-tester", "gh-666");
        user = userRepository.save(user);
        userId = user.getId();
        githubAccountRepository.save(GithubAccount.create(user, "circuit-tester"));
    }

    @Test
    void 연속_실패하면_CircuitBreaker가_OPEN_상태로_전환된다() {
        // given
        CircuitBreaker geminiCircuitBreaker = circuitBreakerRegistry.circuitBreaker("gemini");
        geminiCircuitBreaker.reset();

        // when — 실제 GeminiCareerGapAnalysisClient가 localhost:1(연결 거부)로 호출을 시도
        for (int i = 0; i < 6; i++) {
            String key = "circuit-key-" + UUID.randomUUID();
            var response = careerAnalysisService.triggerAndAccept(userId, key);

            await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
                var analysis = careerAnalysisRepository.findById(response.id()).orElseThrow();
                assertThat(analysis.getStatus().name()).isIn("FAILED", "COMPLETED");
            });
        }

        // then
        assertThat(geminiCircuitBreaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);
    }
}