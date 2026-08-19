package com.devpilot.service;

import com.devpilot.domain.Skill;
import com.devpilot.domain.SkillCategory;
import com.devpilot.domain.User;
import com.devpilot.dto.request.CreateSkillRequest;
import com.devpilot.dto.request.ReorderSkillRequest;
import com.devpilot.dto.request.UpdateSkillRequest;
import com.devpilot.dto.response.SkillResponse;
import com.devpilot.global.exception.SkillNotFoundException;
import com.devpilot.global.exception.UserNotFoundException;
import com.devpilot.repository.SkillCategoryRepository;
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
public class SkillService {

    private final UserRepository userRepository;
    private final SkillRepository skillRepository;
    private final SkillCategoryRepository skillCategoryRepository;

    @Transactional(readOnly = true)
    public List<SkillResponse> getMySkills(Long userId) {
        User user = getUser(userId);
        return skillRepository.findByUserOrderByDisplayOrderAsc(user).stream()
                .map(SkillResponse::from)
                .toList();
    }

    @Transactional
    public SkillResponse createSkill(Long userId, CreateSkillRequest request) {
        User user = getUser(userId);
        SkillCategory category = resolveCategory(request.categoryId());

        int nextOrder = skillRepository.findByUserOrderByDisplayOrderAsc(user).size();

        Skill skill = Skill.create(
                user, request.name(), category,
                request.status(), request.proficiency(), nextOrder
        );

        return SkillResponse.from(skillRepository.save(skill));
    }

    @Transactional
    public SkillResponse updateSkill(Long userId, Long skillId, UpdateSkillRequest request) {
        User user = getUser(userId);
        Skill skill = skillRepository.findByIdAndUser(skillId, user)
                .orElseThrow(() -> new SkillNotFoundException(skillId));

        SkillCategory category = resolveCategory(request.categoryId());
        skill.update(request.name(), category, request.status(), request.proficiency());

        return SkillResponse.from(skill);
    }

    @Transactional
    public void deleteSkill(Long userId, Long skillId) {
        User user = getUser(userId);
        Skill skill = skillRepository.findByIdAndUser(skillId, user)
                .orElseThrow(() -> new SkillNotFoundException(skillId));

        skillRepository.delete(skill);
    }

    @Transactional
    public void reorderSkills(Long userId, ReorderSkillRequest request) {
        User user = getUser(userId);
        List<Skill> mySkills = skillRepository.findByUserOrderByDisplayOrderAsc(user);

        Map<Long, Skill> skillById = mySkills.stream()
                .collect(Collectors.toMap(Skill::getId, s -> s));

        List<Long> orderedIds = request.orderedSkillIds();

        // 본인 소유가 아닌 id가 섞여있으면 거부 (다른 유저 스킬을 조작하는 시도 방지)
        if (!skillById.keySet().containsAll(orderedIds) || orderedIds.size() != mySkills.size()) {
            throw new IllegalArgumentException("잘못된 스킬 목록입니다.");
        }

        for (int i = 0; i < orderedIds.size(); i++) {
            skillById.get(orderedIds.get(i)).changeOrder(i);
        }
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    private SkillCategory resolveCategory(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        return skillCategoryRepository.findById(categoryId).orElse(null);
    }
}