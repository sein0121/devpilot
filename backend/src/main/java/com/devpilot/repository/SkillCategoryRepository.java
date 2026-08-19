package com.devpilot.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.devpilot.domain.SkillCategory;
import com.devpilot.domain.User;

public interface SkillCategoryRepository extends JpaRepository<SkillCategory, Long> {
    List<SkillCategory> findByUserIsNullOrUser(User user);
    
}
