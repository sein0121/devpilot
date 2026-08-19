package com.devpilot.service;

import com.devpilot.domain.SkillCategory;
import com.devpilot.domain.User;
import com.devpilot.dto.response.SkillCategoryResponse;
import com.devpilot.global.exception.UserNotFoundException;
import com.devpilot.repository.SkillCategoryRepository;
import com.devpilot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SkillCategoryService {

    private final UserRepository userRepository;
    private final SkillCategoryRepository skillCategoryRepository;

    @Transactional(readOnly = true)
    public List<SkillCategoryResponse> getCategories(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        return skillCategoryRepository.findByUserIsNullOrUser(user).stream()
                .map(SkillCategoryResponse::from)
                .toList();
    }
}