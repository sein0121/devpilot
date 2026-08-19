package com.devpilot.controller;

import com.devpilot.dto.response.SkillCategoryResponse;
import com.devpilot.global.response.ApiResponse;
import com.devpilot.global.security.DevPilotOAuth2User;
import com.devpilot.service.SkillCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/skill-categories")
@RequiredArgsConstructor
public class SkillCategoryController {

    private final SkillCategoryService skillCategoryService;

    @GetMapping
    public ApiResponse<List<SkillCategoryResponse>> getCategories(
            @AuthenticationPrincipal DevPilotOAuth2User principal
    ) {
        return ApiResponse.success(skillCategoryService.getCategories(principal.getUserId()));
    }
}