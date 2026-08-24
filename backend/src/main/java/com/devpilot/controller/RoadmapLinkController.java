package com.devpilot.controller;

import com.devpilot.dto.request.CreateRoadmapLinkRequest;
import com.devpilot.dto.response.RoadmapLinkResponse;
import com.devpilot.global.response.ApiResponse;
import com.devpilot.global.security.DevPilotOAuth2User;
import com.devpilot.service.RoadmapService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/roadmaps/{roadmapId}/links")
@RequiredArgsConstructor
public class RoadmapLinkController {

    private final RoadmapService roadmapService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<RoadmapLinkResponse> addLink(
            @AuthenticationPrincipal DevPilotOAuth2User principal,
            @PathVariable Long roadmapId,
            @Valid @RequestBody CreateRoadmapLinkRequest request
    ) {
        return ApiResponse.success(roadmapService.addLink(principal.getUserId(), roadmapId, request));
    }

    @DeleteMapping("/{linkId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLink(
            @AuthenticationPrincipal DevPilotOAuth2User principal,
            @PathVariable Long roadmapId,
            @PathVariable Long linkId
    ) {
        roadmapService.deleteLink(principal.getUserId(), roadmapId, linkId);
    }
}