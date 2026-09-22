package com.devpilot.service;

import com.devpilot.domain.CareerAnalysis;
import com.devpilot.dto.response.CareerAnalysisResponse;
import com.devpilot.global.exception.CareerAnalysisNotFoundException;
import com.devpilot.repository.CareerAnalysisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CareerAnalysisService {

    private final CareerAnalysisRepository repository;
    private final CareerAnalysisWriter writer;
    private final CareerAnalysisJobRunner jobRunner;

    public CareerAnalysisResponse triggerAndAccept(Long userId, String idempotencyKey) {
        CareerAnalysis analysis;
        boolean created;

        try {
            analysis = writer.insertPending(userId, idempotencyKey);
            created = true;
        } catch (DataIntegrityViolationException e) {
            analysis = repository.findByUserIdAndIdempotencyKey(userId, idempotencyKey)
                    .orElseThrow(() -> e);
            created = false;
        }

        if (created) {
            jobRunner.runAnalysis(analysis.getId());
        }

        return CareerAnalysisResponse.from(analysis);
    }

    public CareerAnalysisResponse getOwned(Long userId, Long id) {
        CareerAnalysis analysis = repository.findById(id)
                .orElseThrow(() -> new CareerAnalysisNotFoundException(id));

        if (!analysis.getUserId().equals(userId)) {
            throw new CareerAnalysisNotFoundException(id); // 403 대신 404 — 존재 여부 비노출
        }

        return CareerAnalysisResponse.from(analysis);
    }
}