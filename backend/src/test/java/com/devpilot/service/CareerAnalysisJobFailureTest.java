package com.devpilot.service;

import com.devpilot.domain.CareerAnalysisStatus;
import com.devpilot.domain.GithubAccount;
import com.devpilot.domain.User;
import com.devpilot.repository.CareerAnalysisRepository;
import com.devpilot.repository.GithubAccountRepository;
import com.devpilot.repository.UserRepository;
import com.devpilot.service.ai.AiCareerGapAnalysisClient;
import com.devpilot.support.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@Import(CareerAnalysisJobFailureTest.FailingAiConfig.class)
class CareerAnalysisJobFailureTest extends AbstractIntegrationTest {

    @Autowired
    private CareerAnalysisService careerAnalysisService;

    @Autowired
    private CareerAnalysisRepository careerAnalysisRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GithubAccountRepository githubAccountRepository;

    private Long userId;

    @BeforeEach
    void setUp() {
        User user = User.createGithubUser("failure-test@devpilot.com", "failure-tester", "gh-888");
        user = userRepository.save(user);
        userId = user.getId();

        githubAccountRepository.save(GithubAccount.create(user, "failure-tester"));
    }

    @Test
    void AI_호출이_실패하면_FAILED_상태와_에러메시지가_기록된다() {
        // given
        String idempotencyKey = "failure-key-" + UUID.randomUUID();

        // when
        var response = careerAnalysisService.triggerAndAccept(userId, idempotencyKey);

        // then
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            var analysis = careerAnalysisRepository.findById(response.id()).orElseThrow();
            assertThat(analysis.getStatus()).isEqualTo(CareerAnalysisStatus.FAILED);
            assertThat(analysis.getErrorMessage()).contains("의도적 테스트 실패");
            assertThat(analysis.getResult()).isNull();
            assertThat(analysis.getStartedAt()).isNotNull();
            assertThat(analysis.getCompletedAt()).isNotNull();
        });
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class FailingAiConfig {
        @Bean
        @Primary
        AiCareerGapAnalysisClient failingAiCareerGapAnalysisClient() {
            return context -> {
                throw new RuntimeException("의도적 테스트 실패");
            };
        }
    }
}