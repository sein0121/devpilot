//user = null인 기본 카테고리(Backend, Frontend 등)를 앱 시작 시 자동으로 채워 넣음 
// 앱이 시작된 후 딱 한번만 실행
package com.devpilot.global.config;

import com.devpilot.domain.SkillCategory;
import com.devpilot.repository.SkillCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class SkillCategorySeeder implements ApplicationRunner {

    private final SkillCategoryRepository skillCategoryRepository;

    private static final Map<String, List<String>> DEFAULT_CATEGORIES = Map.of(
            "Backend", List.of("Java", "Spring", "Python", "Node.js"),
            "Frontend", List.of("React", "TypeScript", "Vue"),
            "Database", List.of("MySQL", "Redis", "MongoDB"),
            "DevOps", List.of("Docker", "Kubernetes", "AWS")
    );

    @Override
    public void run(ApplicationArguments args) {
        if (skillCategoryRepository.count() > 0) {
            return; // 이미 시딩됐으면 중복 실행 방지
        }

        DEFAULT_CATEGORIES.forEach((parentName, children) -> {
            SkillCategory parent = skillCategoryRepository.save(
                    SkillCategory.createDefault(parentName, null)
            );
            children.forEach(childName ->
                    skillCategoryRepository.save(SkillCategory.createDefault(childName, parent))
            );
        });
    }
}