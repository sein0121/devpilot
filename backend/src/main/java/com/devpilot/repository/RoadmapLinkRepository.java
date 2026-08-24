package com.devpilot.repository;

import com.devpilot.domain.Roadmap;
import com.devpilot.domain.RoadmapLink;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoadmapLinkRepository extends JpaRepository<RoadmapLink, Long> {
    List<RoadmapLink> findByRoadmapOrderByIdAsc(Roadmap roadmap);
    Optional<RoadmapLink> findByIdAndRoadmap(Long id, Roadmap roadmap);
    void deleteByRoadmap(Roadmap roadmap);
}