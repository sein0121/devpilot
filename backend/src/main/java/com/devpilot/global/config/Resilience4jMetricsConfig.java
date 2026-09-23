package com.devpilot.global.config;

import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.micrometer.tagged.TaggedBulkheadMetrics;
import io.github.resilience4j.micrometer.tagged.TaggedCircuitBreakerMetrics;
import io.github.resilience4j.micrometer.tagged.TaggedRetryMetrics;
import io.github.resilience4j.retry.RetryRegistry;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Resilience4j의 자동설정(CircuitBreakerMetricsAutoConfiguration 등)이
 * MeterRegistry Bean 생성 시점보다 먼저 조건을 평가해버려서
 * @ConditionalOnBean(MeterRegistry) 체크가 실패하는 알려진 타이밍 문제 회피용.
 * (resilience4j/resilience4j#1563)
 * 여기서 명시적으로 Bean을 등록하면, 의존성 주입 시점에 MeterRegistry가
 * 필요에 따라 먼저 생성되므로 순서 문제가 발생하지 않는다.
 */
@Configuration
public class Resilience4jMetricsConfig {

    @Bean
    public TaggedCircuitBreakerMetrics taggedCircuitBreakerMetrics(
            CircuitBreakerRegistry circuitBreakerRegistry, MeterRegistry meterRegistry
    ) {
        TaggedCircuitBreakerMetrics metrics = TaggedCircuitBreakerMetrics.ofCircuitBreakerRegistry(circuitBreakerRegistry);
        metrics.bindTo(meterRegistry);
        return metrics;
    }

    @Bean
    public TaggedRetryMetrics taggedRetryMetrics(
            RetryRegistry retryRegistry, MeterRegistry meterRegistry
    ) {
        TaggedRetryMetrics metrics = TaggedRetryMetrics.ofRetryRegistry(retryRegistry);
        metrics.bindTo(meterRegistry);
        return metrics;
    }

    @Bean
    public TaggedBulkheadMetrics taggedBulkheadMetrics(
            BulkheadRegistry bulkheadRegistry, MeterRegistry meterRegistry
    ) {
        TaggedBulkheadMetrics metrics = TaggedBulkheadMetrics.ofBulkheadRegistry(bulkheadRegistry);
        metrics.bindTo(meterRegistry);
        return metrics;
    }
}