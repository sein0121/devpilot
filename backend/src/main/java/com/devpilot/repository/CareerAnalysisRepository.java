package com.devpilot.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.devpilot.domain.CareerAnalysis;

public interface CareerAnalysisRepository extends JpaRepository<CareerAnalysis, Long> {
    Optional<CareerAnalysis> findByUserIdAndIdempotencyKey(Long userId, String idempotencyKey);
}
