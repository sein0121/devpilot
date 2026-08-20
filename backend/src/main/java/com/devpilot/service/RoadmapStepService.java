package com.devpilot.service;

import com.devpilot.domain.Roadmap;
import com.devpilot.domain.RoadmapStep;
import com.devpilot.domain.Skill;
import com.devpilot.domain.User;
import com.devpilot.dto.request.CreateRoadmapStepRequest;
import com.devpilot.dto.request.ReorderStepsRequest;
import com.devpilot.dto.request.UpdateRoadmapStepRequest;
import com.devpilot.dto.request.UpdateStepStatusRequest;
import com.devpilot.dto.response.RoadmapStepResponse;
import com.devpilot.global.exception.RoadmapNotFoundException;
import com.devpilot.global.exception.RoadmapStepNotFoundException;
import com.devpilot.global.exception.SkillNotFoundException;
import com.devpilot.global.exception.UserNotFoundException;
import com.devpilot.repository.RoadmapRepository;
import com.devpilot.repository.RoadmapStepRepository;
import com.devpilot.repository.SkillRepository;
import com.devpilot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoadmapStepService {

    private final UserRepository userRepository;
    private final RoadmapRepository roadmapRepository;
    private final RoadmapStepRepository roadmapStepRepository;
    private final SkillRepository skillRepository;

    @Transactional
    public RoadmapStepResponse createStep(Long userId, Long roadmapId, CreateRoadmapStepRequest request) {
        User user = getUser(userId);
        Roadmap roadmap = getOwnedRoadmap(user, roadmapId);
        Skill skill = skillRepository.findByIdAndUser(request.skillId(), user)
                .orElseThrow(() -> new SkillNotFoundException(request.skillId()));

        int nextOrder = roadmapStepRepository.findByRoadmapOrderByDisplayOrderAsc(roadmap).size();

        RoadmapStep step = roadmapStepRepository.save(
                RoadmapStep.create(roadmap, skill, request.title(), request.description(),
                        request.targetDate(), nextOrder)
        );

        return RoadmapStepResponse.from(step);
    }

    @Transactional
    public RoadmapStepResponse updateStep(
            Long userId, Long roadmapId, Long stepId, UpdateRoadmapStepRequest request
    ) {
        User user = getUser(userId);
        Roadmap roadmap = getOwnedRoadmap(user, roadmapId);
        RoadmapStep step = getOwnedStep(roadmap, stepId);

        Skill skill = null;
        if (request.skillId() != null) {
            skill = skillRepository.findByIdAndUser(request.skillId(), user)
                    .orElseThrow(() -> new SkillNotFoundException(request.skillId()));
        }

        step.update(request.title(), request.description(), request.targetDate(), skill);
        return RoadmapStepResponse.from(step);
    }

    @Transactional
    public RoadmapStepResponse updateStatus(
            Long userId, Long roadmapId, Long stepId, UpdateStepStatusRequest request
    ) {
        User user = getUser(userId);
        Roadmap roadmap = getOwnedRoadmap(user, roadmapId);
        RoadmapStep step = getOwnedStep(roadmap, stepId);

        step.changeStatus(request.status());
        return RoadmapStepResponse.from(step);
    }

    @Transactional
    public void deleteStep(Long userId, Long roadmapId, Long stepId) {
        User user = getUser(userId);
        Roadmap roadmap = getOwnedRoadmap(user, roadmapId);
        RoadmapStep step = getOwnedStep(roadmap, stepId);
        roadmapStepRepository.delete(step);
    }

    @Transactional
    public void reorderSteps(Long userId, Long roadmapId, ReorderStepsRequest request) {
        User user = getUser(userId);
        Roadmap roadmap = getOwnedRoadmap(user, roadmapId);
        List<RoadmapStep> steps = roadmapStepRepository.findByRoadmapOrderByDisplayOrderAsc(roadmap);

        Map<Long, RoadmapStep> stepById = steps.stream()
                .collect(Collectors.toMap(RoadmapStep::getId, s -> s));

        List<Long> orderedIds = request.orderedStepIds();

        if (!stepById.keySet().containsAll(orderedIds) || orderedIds.size() != steps.size()) {
            throw new IllegalArgumentException("잘못된 스텝 목록입니다.");
        }

        for (int i = 0; i < orderedIds.size(); i++) {
            stepById.get(orderedIds.get(i)).changeOrder(i);
        }
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    private Roadmap getOwnedRoadmap(User user, Long roadmapId) {
        return roadmapRepository.findByIdAndUser(roadmapId, user)
                .orElseThrow(() -> new RoadmapNotFoundException(roadmapId));
    }

    private RoadmapStep getOwnedStep(Roadmap roadmap, Long stepId) {
        return roadmapStepRepository.findByIdAndRoadmap(stepId, roadmap)
                .orElseThrow(() -> new RoadmapStepNotFoundException(stepId));
    }
}