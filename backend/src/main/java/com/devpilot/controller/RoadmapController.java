package com.devpilot.controller;

import com.devpilot.dto.request.UpsertRoadmapRequest;
import com.devpilot.dto.response.RoadmapDetailResponse;
import com.devpilot.dto.response.RoadmapSummaryResponse;
import com.devpilot.global.response.ApiResponse;
import com.devpilot.global.security.DevPilotOAuth2User;
import com.devpilot.service.RoadmapService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roadmaps")
@RequiredArgsConstructor
public class RoadmapController {

    private final RoadmapService roadmapService;

    @GetMapping
    public ApiResponse<List<RoadmapSummaryResponse>> getMyRoadmaps(
            @AuthenticationPrincipal DevPilotOAuth2User principal
    ) {
        return ApiResponse.success(roadmapService.getMyRoadmaps(principal.getUserId()));
    }

    @GetMapping("/{id}")
    public ApiResponse<RoadmapDetailResponse> getRoadmapDetail(
            @AuthenticationPrincipal DevPilotOAuth2User principal,
            @PathVariable Long id
    ) {
        return ApiResponse.success(roadmapService.getRoadmapDetail(principal.getUserId(), id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<RoadmapSummaryResponse> createRoadmap(
            @AuthenticationPrincipal DevPilotOAuth2User principal,
            @Valid @RequestBody UpsertRoadmapRequest request
    ) {
        return ApiResponse.success(roadmapService.createRoadmap(principal.getUserId(), request));
    }

    @PutMapping("/{id}")
    public ApiResponse<RoadmapSummaryResponse> updateRoadmap(
            @AuthenticationPrincipal DevPilotOAuth2User principal,
            @PathVariable Long id,
            @Valid @RequestBody UpsertRoadmapRequest request
    ) {
        return ApiResponse.success(roadmapService.updateRoadmap(principal.getUserId(), id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRoadmap(
            @AuthenticationPrincipal DevPilotOAuth2User principal,
            @PathVariable Long id
    ) {
        roadmapService.deleteRoadmap(principal.getUserId(), id);
    }
}