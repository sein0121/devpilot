package com.devpilot.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.binder.MeterBinder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Career Gap Analysis Job의 생애주기를 관찰하기 위한 커스텀 지표.
 * 계측 포인트가 여기저기 흩어지지 않도록 이 클래스에서만 MeterRegistry를 직접 다룬다.
 */
@Component
@RequiredArgsConstructor
public class CareerAnalysisMetrics {

    private final MeterRegistry meterRegistry;

    private final AtomicInteger queueSize = new AtomicInteger(0);

    private Counter requestedCounter;
    private Counter completedCounter;
    private Counter failedCounter;
    private Timer queueWaitTimer;
    private Timer executionTimer;

    @jakarta.annotation.PostConstruct
    void init() {
        requestedCounter = Counter.builder("career.analysis.requested")
                .description("Career Gap Analysis 요청 수 (Idempotency 재사용 응답 포함)")
                .register(meterRegistry);

        completedCounter = Counter.builder("career.analysis.completed")
                .description("성공적으로 완료된 Career Gap Analysis 수")
                .register(meterRegistry);

        failedCounter = Counter.builder("career.analysis.failed")
                .description("실패한 Career Gap Analysis 수")
                .register(meterRegistry);

        queueWaitTimer = Timer.builder("career.analysis.queue.wait.time")
                .description("생성부터 실제 실행 시작까지 걸린 대기 시간")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry);

        executionTimer = Timer.builder("career.analysis.execution.time")
                .description("실행 시작부터 완료(성공/실패)까지 걸린 시간")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry);

        meterRegistry.gauge("career.analysis.queue.size", queueSize);
    }

    public void recordRequested() {
        requestedCounter.increment();
    }

    public void recordCompleted() {
        completedCounter.increment();
    }

    public void recordFailed() {
        failedCounter.increment();
    }

    public void recordQueueWait(Duration waitTime) {
        queueWaitTimer.record(waitTime);
    }

    public void recordExecutionTime(Duration executionTime) {
        executionTimer.record(executionTime);
    }

    public void incrementQueueSize() {
        queueSize.incrementAndGet();
    }

    public void decrementQueueSize() {
        queueSize.updateAndGet(v -> Math.max(0, v - 1));
    }
}