package com.devpilot.service;

import com.devpilot.domain.Roadmap;
import com.devpilot.domain.RoadmapLink;
import com.devpilot.domain.RoadmapStep;
import com.devpilot.domain.User;
import com.devpilot.dto.request.CreateRoadmapLinkRequest;
import com.devpilot.dto.request.UpsertRoadmapRequest;
import com.devpilot.dto.response.RoadmapDetailResponse;
import com.devpilot.dto.response.RoadmapLinkResponse;
import com.devpilot.dto.response.RoadmapSummaryResponse;
import com.devpilot.global.exception.RoadmapLinkNotFoundException;
import com.devpilot.global.exception.RoadmapNotFoundException;
import com.devpilot.global.exception.UserNotFoundException;
import com.devpilot.repository.RoadmapLinkRepository;
import com.devpilot.repository.RoadmapRepository;
import com.devpilot.repository.RoadmapStepRepository;
import com.devpilot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoadmapService {

    private final UserRepository userRepository;
    private final RoadmapRepository roadmapRepository;
    private final RoadmapStepRepository roadmapStepRepository;
    private final RoadmapLinkRepository roadmapLinkRepository;

    @Transactional(readOnly = true)
    public List<RoadmapSummaryResponse> getMyRoadmaps(Long userId) {
        User user = getUser(userId);
        return roadmapRepository.findByUserOrderByIdDesc(user).stream()
                .map(roadmap -> RoadmapSummaryResponse.of(
                        roadmap, roadmapStepRepository.findByRoadmapOrderByDisplayOrderAsc(roadmap)
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public RoadmapDetailResponse getRoadmapDetail(Long userId, Long roadmapId) {
        User user = getUser(userId);
        Roadmap roadmap = getOwnedRoadmap(user, roadmapId);
        List<RoadmapStep> steps = roadmapStepRepository.findByRoadmapOrderByDisplayOrderAsc(roadmap);
        List<RoadmapLink> links = roadmapLinkRepository.findByRoadmapOrderByIdAsc(roadmap); // 추가
        return RoadmapDetailResponse.of(roadmap, steps, links); // links 인자 추가
    }

    @Transactional
    public RoadmapSummaryResponse createRoadmap(Long userId, UpsertRoadmapRequest request) {
        User user = getUser(userId);
        Roadmap roadmap = roadmapRepository.save(
                Roadmap.create(user, request.title(), request.description(), request.targetDate())
        );
        return RoadmapSummaryResponse.of(roadmap, List.of());
    }

    @Transactional
    public RoadmapSummaryResponse updateRoadmap(Long userId, Long roadmapId, UpsertRoadmapRequest request) {
        User user = getUser(userId);
        Roadmap roadmap = getOwnedRoadmap(user, roadmapId);
        roadmap.update(request.title(), request.description(), request.targetDate());

        List<com.devpilot.domain.RoadmapStep> steps =
                roadmapStepRepository.findByRoadmapOrderByDisplayOrderAsc(roadmap);
        return RoadmapSummaryResponse.of(roadmap, steps);
    }

    @Transactional
    public void deleteRoadmap(Long userId, Long roadmapId) {
        User user = getUser(userId);
        Roadmap roadmap = getOwnedRoadmap(user, roadmapId);
        roadmapStepRepository.deleteByRoadmap(roadmap);
        roadmapLinkRepository.deleteByRoadmap(roadmap); // 추가 — 링크도 같이 정리
        roadmapRepository.delete(roadmap);
    }

    @Transactional
    public RoadmapLinkResponse addLink(Long userId, Long roadmapId, CreateRoadmapLinkRequest request) {
        User user = getUser(userId);
        Roadmap roadmap = getOwnedRoadmap(user, roadmapId);

        RoadmapLink link = roadmapLinkRepository.save(
                RoadmapLink.create(roadmap, request.url(), request.label())
        );
        return RoadmapLinkResponse.from(link);
    }

    @Transactional
    public void deleteLink(Long userId, Long roadmapId, Long linkId) {
        User user = getUser(userId);
        Roadmap roadmap = getOwnedRoadmap(user, roadmapId);
        RoadmapLink link = roadmapLinkRepository.findByIdAndRoadmap(linkId, roadmap)
                .orElseThrow(() -> new RoadmapLinkNotFoundException(linkId));
        roadmapLinkRepository.delete(link);
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    private Roadmap getOwnedRoadmap(User user, Long roadmapId) {
        return roadmapRepository.findByIdAndUser(roadmapId, user)
                .orElseThrow(() -> new RoadmapNotFoundException(roadmapId));
    }
}