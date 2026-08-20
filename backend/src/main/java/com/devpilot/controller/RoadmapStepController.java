// controller/RoadmapStepController.java
package com.devpilot.controller;

import com.devpilot.dto.request.CreateRoadmapStepRequest;
import com.devpilot.dto.request.ReorderStepsRequest;
import com.devpilot.dto.request.UpdateRoadmapStepRequest;
import com.devpilot.dto.request.UpdateStepStatusRequest;
import com.devpilot.dto.response.RoadmapStepResponse;
import com.devpilot.global.response.ApiResponse;
import com.devpilot.global.security.DevPilotOAuth2User;
import com.devpilot.service.RoadmapStepService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/roadmaps/{roadmapId}/steps")
@RequiredArgsConstructor
public class RoadmapStepController {

    private final RoadmapStepService roadmapStepService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<RoadmapStepResponse> createStep(
            @AuthenticationPrincipal DevPilotOAuth2User principal,
            @PathVariable Long roadmapId,
            @Valid @RequestBody CreateRoadmapStepRequest request
    ) {
        return ApiResponse.success(
                roadmapStepService.createStep(principal.getUserId(), roadmapId, request)
        );
    }

    @PutMapping("/{stepId}")
    public ApiResponse<RoadmapStepResponse> updateStep(
            @AuthenticationPrincipal DevPilotOAuth2User principal,
            @PathVariable Long roadmapId,
            @PathVariable Long stepId,
            @Valid @RequestBody UpdateRoadmapStepRequest request
    ) {
        return ApiResponse.success(
                roadmapStepService.updateStep(principal.getUserId(), roadmapId, stepId, request)
        );
    }

    @PatchMapping("/{stepId}/status")
    public ApiResponse<RoadmapStepResponse> updateStatus(
            @AuthenticationPrincipal DevPilotOAuth2User principal,
            @PathVariable Long roadmapId,
            @PathVariable Long stepId,
            @Valid @RequestBody UpdateStepStatusRequest request
    ) {
        return ApiResponse.success(
                roadmapStepService.updateStatus(principal.getUserId(), roadmapId, stepId, request)
        );
    }

    @DeleteMapping("/{stepId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteStep(
            @AuthenticationPrincipal DevPilotOAuth2User principal,
            @PathVariable Long roadmapId,
            @PathVariable Long stepId
    ) {
        roadmapStepService.deleteStep(principal.getUserId(), roadmapId, stepId);
    }

    @PutMapping("/reorder")
    public ApiResponse<Void> reorderSteps(
            @AuthenticationPrincipal DevPilotOAuth2User principal,
            @PathVariable Long roadmapId,
            @Valid @RequestBody ReorderStepsRequest request
    ) {
        roadmapStepService.reorderSteps(principal.getUserId(), roadmapId, request);
        return ApiResponse.success(null);
    }
}