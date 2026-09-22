package com.devpilot.domain;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "career_analysis")
public class CareerAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "idempotency_key", nullable = false)
    private String idempotencyKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CareerAnalysisStatus status;

    @Lob
    private String result;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    private LocalDateTime startedAt;
    private LocalDateTime completedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected CareerAnalysis() {}

    public static CareerAnalysis createPending(Long userId, String idempotencyKey) {
        CareerAnalysis analysis = new CareerAnalysis();
        analysis.userId = userId;
        analysis.idempotencyKey = idempotencyKey;
        analysis.status = CareerAnalysisStatus.PENDING;
        LocalDateTime now = LocalDateTime.now();
        analysis.createdAt = now;
        analysis.updatedAt = now;
        return analysis;
    }

    public void markRunning() {
        this.status = CareerAnalysisStatus.RUNNING;
        this.startedAt = LocalDateTime.now();
        this.updatedAt = this.startedAt;
    }

    public void markCompleted(String result) {
        this.status = CareerAnalysisStatus.COMPLETED;
        this.result = result;
        this.completedAt = LocalDateTime.now();
        this.updatedAt = this.completedAt;
    }

    public void markFailed(String errorMessage) {
        this.status = CareerAnalysisStatus.FAILED;
        this.errorMessage = errorMessage;
        this.completedAt = LocalDateTime.now();
        this.updatedAt = this.completedAt;
    }

    // getters 생략
}
