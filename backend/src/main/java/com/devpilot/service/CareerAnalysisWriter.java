package com.devpilot.service;

import com.devpilot.repository.CareerAnalysisRepository;
import com.devpilot.domain.CareerAnalysis;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
class CareerAnalysisWriter {

    private final CareerAnalysisRepository repository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public CareerAnalysis insertPending(Long userId, String idempotencyKey) {
        return repository.save(CareerAnalysis.createPending(userId, idempotencyKey));
    }

    @Transactional
    public void markRunning(Long analysisId) {
        repository.findById(analysisId).ifPresent(CareerAnalysis::markRunning);
    }

    @Transactional
    public void markCompleted(Long analysisId, String result) {
        repository.findById(analysisId).ifPresent(a -> a.markCompleted(result));
    }

    @Transactional
    public void markFailed(Long analysisId, String errorMessage) {
        repository.findById(analysisId).ifPresent(a -> a.markFailed(errorMessage));
    }
}