package com.devpilot.service.ai;

import com.devpilot.domain.Skill;
import com.devpilot.domain.User;
import com.devpilot.dto.request.GenerateRoadmapDraftRequest;
import com.devpilot.dto.response.RoadmapDraftResponse;
import com.devpilot.dto.response.RoadmapStepDraftResponse;
import com.devpilot.global.exception.UserNotFoundException;
import com.devpilot.repository.SkillRepository;
import com.devpilot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AiRoadmapService {

    private final UserRepository userRepository;
    private final SkillRepository skillRepository;
    private final AiRoadmapDraftClient aiRoadmapDraftClient; // 인터페이스만 의존 — Gemini/OpenAI 상관없음

    public RoadmapDraftResponse generateDraft(Long userId, GenerateRoadmapDraftRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        List<Skill> mySkills = skillRepository.findByUserOrderByDisplayOrderAsc(user);
        List<String> skillNames = mySkills.stream().map(Skill::getName).toList();

        List<AiStepDraft> drafts = aiRoadmapDraftClient.generateDraft(
                request.goal(), request.targetDate(), skillNames
        );

        Map<String, Long> skillNameToId = mySkills.stream()
                .collect(Collectors.toMap(
                        s -> s.getName().toLowerCase(),
                        Skill::getId,
                        (existing, duplicate) -> existing // 이름 중복 시 첫 번째 것 유지
                ));

        List<RoadmapStepDraftResponse> steps = drafts.stream()
                .map(draft -> new RoadmapStepDraftResponse(
                        draft.title(),
                        draft.description(),
                        draft.suggestedSkillName(),
                        skillNameToId.get(draft.suggestedSkillName().toLowerCase()) // 못 찾으면 null
                ))
                .toList();

        return new RoadmapDraftResponse(steps);
    }
}