package com.devpilot.controller;

import com.devpilot.dto.request.GenerateRoadmapDraftRequest;
import com.devpilot.dto.response.RoadmapDraftResponse;
import com.devpilot.global.response.ApiResponse;
import com.devpilot.global.security.DevPilotOAuth2User;
import com.devpilot.service.ai.AiRoadmapService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiRoadmapController {

    private final AiRoadmapService aiRoadmapService;

    @PostMapping("/roadmap-drafts")
    public ApiResponse<RoadmapDraftResponse> generateDraft(
            @AuthenticationPrincipal DevPilotOAuth2User principal,
            @Valid @RequestBody GenerateRoadmapDraftRequest request
    ) {
        return ApiResponse.success(aiRoadmapService.generateDraft(principal.getUserId(), request));
    }
}