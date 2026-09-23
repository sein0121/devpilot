package com.devpilot.service;

import com.devpilot.domain.AuthProvider;
import com.devpilot.domain.CareerAnalysisStatus;
import com.devpilot.domain.User;
import com.devpilot.domain.Role;
import com.devpilot.repository.CareerAnalysisRepository;
import com.devpilot.repository.GithubAccountRepository;
import com.devpilot.repository.UserRepository;
import com.devpilot.domain.GithubAccount;
import com.devpilot.support.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import static org.awaitility.Awaitility.await;

import static org.assertj.core.api.Assertions.assertThat;

class CareerAnalysisServiceConcurrencyTest extends AbstractIntegrationTest {

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
        User user = User.createGithubUser("concurrency-test@devpilot.com", "concurrency-tester", "gh-999");
        user = userRepository.save(user);
        userId = user.getId();

        githubAccountRepository.save(GithubAccount.create(user, "concurrency-tester"));
    }

    @Test
    void 동시에_같은_Idempotency_Key로_요청해도_레코드는_하나만_생성된다() throws InterruptedException {
        // given
        String idempotencyKey = "concurrent-key-" + UUID.randomUUID();
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failureCount = new AtomicInteger();
        List<Long> resultIds = new ArrayList<>();

        // when — 모든 스레드가 동시에 출발하도록 래치로 정렬
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                readyLatch.countDown();
                try {
                    startLatch.await(); // 전체 스레드가 준비될 때까지 대기
                    var response = careerAnalysisService.triggerAndAccept(userId, idempotencyKey);
                    synchronized (resultIds) {
                        resultIds.add(response.id());
                    }
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await(5, TimeUnit.SECONDS);
        startLatch.countDown(); // 동시 출발
        doneLatch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        // then
        assertThat(successCount.get()).isEqualTo(threadCount); // 전부 정상 응답(에러로 죽은 스레드 없음)
        assertThat(failureCount.get()).isZero();
        assertThat(resultIds).allMatch(id -> id.equals(resultIds.get(0))); // 모든 응답이 같은 id를 가리킴
        // then 섹션 마지막에
        org.awaitility.Awaitility.await()
        .atMost(10, TimeUnit.SECONDS)
        .until(() -> {
            var analysis = careerAnalysisRepository.findById(resultIds.get(0)).orElseThrow();
            return analysis.getStatus() == CareerAnalysisStatus.COMPLETED
                    || analysis.getStatus() == CareerAnalysisStatus.FAILED;
        });

        long actualCount = careerAnalysisRepository.findByUserIdAndIdempotencyKey(userId, idempotencyKey)
                .stream().count();
        // findByUserIdAndIdempotencyKey는 Optional이라 count는 0 또는 1이어야 함
        assertThat(actualCount).isEqualTo(1);
    }
}