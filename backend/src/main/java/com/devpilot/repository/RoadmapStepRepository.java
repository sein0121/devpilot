package com.devpilot.repository;

import com.devpilot.domain.Roadmap;
import com.devpilot.domain.RoadmapStep;
import com.devpilot.domain.Skill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoadmapStepRepository extends JpaRepository<RoadmapStep, Long> {
    List<RoadmapStep> findByRoadmapOrderByDisplayOrderAsc(Roadmap roadmap);
    Optional<RoadmapStep> findByIdAndRoadmap(Long id, Roadmap roadmap);
    List<RoadmapStep> findBySkill(Skill skill); // Skill 삭제 시 연결 해제용
    void deleteByRoadmap(Roadmap roadmap); // Roadmap 삭제 시 함께 정리
}