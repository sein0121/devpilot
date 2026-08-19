package com.devpilot.global.config;

import com.devpilot.domain.SkillCategory;
import com.devpilot.repository.SkillCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SkillCategorySeeder implements ApplicationRunner {

    private final SkillCategoryRepository skillCategoryRepository;

    private static final List<String> DEFAULT_CATEGORIES = List.of(
            "Backend", "Frontend", "Database", "DevOps"
    );

    @Override
    public void run(ApplicationArguments args) {
        if (skillCategoryRepository.count() > 0) {
            return;
        }

        DEFAULT_CATEGORIES.forEach(name ->
                skillCategoryRepository.save(SkillCategory.createDefault(name, null))
        );
    }
}