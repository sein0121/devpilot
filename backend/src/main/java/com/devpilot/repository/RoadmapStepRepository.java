package com.devpilot.repository;

import com.devpilot.domain.Roadmap;
import com.devpilot.domain.RoadmapStep;
import com.devpilot.domain.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RoadmapStepRepository extends JpaRepository<RoadmapStep, Long> {
    List<RoadmapStep> findByRoadmapOrderByDisplayOrderAsc(Roadmap roadmap);
    Optional<RoadmapStep> findByIdAndRoadmap(Long id, Roadmap roadmap);
    List<RoadmapStep> findBySkill(Skill skill);
    void deleteByRoadmap(Roadmap roadmap);

    @Query("""
            SELECT rs FROM RoadmapStep rs
            LEFT JOIN FETCH rs.skill
            WHERE rs.roadmap = :roadmap
            ORDER BY rs.displayOrder ASC
            """)
    List<RoadmapStep> findByRoadmapWithSkillOrderByDisplayOrderAsc(@Param("roadmap") Roadmap roadmap);
}