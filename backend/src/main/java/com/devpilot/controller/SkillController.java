package com.devpilot.controller;

import com.devpilot.dto.request.CreateSkillRequest;
import com.devpilot.dto.request.ReorderSkillRequest;
import com.devpilot.dto.request.UpdateSkillRequest;
import com.devpilot.dto.response.SkillResponse;
import com.devpilot.global.response.ApiResponse;
import com.devpilot.global.security.DevPilotOAuth2User;
import com.devpilot.service.SkillService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/skills")
@RequiredArgsConstructor
public class SkillController {

    private final SkillService skillService;

    @GetMapping
    public ApiResponse<List<SkillResponse>> getMySkills(@AuthenticationPrincipal DevPilotOAuth2User principal) {
        return ApiResponse.success(skillService.getMySkills(principal.getUserId()));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<SkillResponse> createSkill(
            @AuthenticationPrincipal DevPilotOAuth2User principal,
            @Valid @RequestBody CreateSkillRequest request
    ) {
        return ApiResponse.success(skillService.createSkill(principal.getUserId(), request));
    }

    @PutMapping("/{id}")
    public ApiResponse<SkillResponse> updateSkill(
            @AuthenticationPrincipal DevPilotOAuth2User principal,
            @PathVariable Long id,
            @Valid @RequestBody UpdateSkillRequest request
    ) {
        return ApiResponse.success(skillService.updateSkill(principal.getUserId(), id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSkill(
            @AuthenticationPrincipal DevPilotOAuth2User principal,
            @PathVariable Long id
    ) {
        skillService.deleteSkill(principal.getUserId(), id);
    }

    @PutMapping("/reorder")
    public ApiResponse<Void> reorderSkills(
            @AuthenticationPrincipal DevPilotOAuth2User principal,
            @Valid @RequestBody ReorderSkillRequest request
    ) {
        skillService.reorderSkills(principal.getUserId(), request);
        return ApiResponse.success(null);
    }
}