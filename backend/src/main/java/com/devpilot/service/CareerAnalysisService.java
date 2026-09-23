package com.devpilot.service;

import com.devpilot.domain.CareerAnalysis;
import com.devpilot.domain.User;
import com.devpilot.dto.response.CareerAnalysisResponse;
import com.devpilot.global.exception.CareerAnalysisNotFoundException;
import com.devpilot.global.exception.GithubAccountRequiredException;
import com.devpilot.repository.CareerAnalysisRepository;
import com.devpilot.repository.GithubAccountRepository;
import com.devpilot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CareerAnalysisService {

    private final CareerAnalysisRepository repository;
    private final CareerAnalysisWriter writer;
    private final CareerAnalysisJobRunner jobRunner;
    private final UserRepository userRepository;
    private final GithubAccountRepository githubAccountRepository;
    private final CareerAnalysisMetrics metrics;

    public CareerAnalysisResponse triggerAndAccept(Long userId, String idempotencyKey) {
        validateGithubLinked(userId);
        metrics.recordRequested();

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
            metrics.incrementQueueSize();
            jobRunner.runAnalysis(analysis.getId(), analysis.getCreatedAt());
        }

        return CareerAnalysisResponse.from(analysis);
    }

    private void validateGithubLinked(Long userId) {
        userRepository.findById(userId).ifPresent(user -> {
            if (githubAccountRepository.findByUser(user).isEmpty()) {
                throw new GithubAccountRequiredException();
            }
        });
    }

    public CareerAnalysisResponse getOwned(Long userId, Long id) {
        CareerAnalysis analysis = repository.findById(id)
                .orElseThrow(() -> new CareerAnalysisNotFoundException(id));

        if (!analysis.getUserId().equals(userId)) {
            throw new CareerAnalysisNotFoundException(id);
        }

        return CareerAnalysisResponse.from(analysis);
    }
}