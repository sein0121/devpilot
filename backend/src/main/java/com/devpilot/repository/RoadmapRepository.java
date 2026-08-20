package com.devpilot.repository;

import com.devpilot.domain.Roadmap;
import com.devpilot.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoadmapRepository extends JpaRepository<Roadmap, Long> {
    List<Roadmap> findByUserOrderByIdDesc(User user);
    Optional<Roadmap> findByIdAndUser(Long id, User user);
}