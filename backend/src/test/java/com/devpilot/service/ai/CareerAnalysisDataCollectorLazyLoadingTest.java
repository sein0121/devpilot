package com.devpilot.service.ai;

import com.devpilot.domain.*;
import com.devpilot.repository.*;
import com.devpilot.service.CareerAnalysisService;
import com.devpilot.support.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@Import(CareerAnalysisDataCollectorLazyLoadingTest.CapturingAiConfig.class)
class CareerAnalysisDataCollectorLazyLoadingTest extends AbstractIntegrationTest {

    @Autowired
    private CareerAnalysisService careerAnalysisService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GithubAccountRepository githubAccountRepository;

    @Autowired
    private SkillCategoryRepository skillCategoryRepository;

    @Autowired
    private SkillRepository skillRepository;

    @Autowired
    private StudyLogRepository studyLogRepository;

    @Autowired
    private RoadmapRepository roadmapRepository;

    @Autowired
    private RoadmapStepRepository roadmapStepRepository;

    @Autowired
    private CapturingAiConfig capturingAiConfig;

    private Long userId;

    @BeforeEach
    void setUp() {
        // 사용자 + GitHub 연동 (428 관문 통과용)
        User user = User.createGithubUser("lazy-test@devpilot.com", "lazy-tester", "gh-777");
        user = userRepository.save(user);
        userId = user.getId();
        githubAccountRepository.save(GithubAccount.create(user, "lazy-tester"));

        // 카테고리 있는 스킬 + 카테고리 없는 스킬
        SkillCategory category = skillCategoryRepository.save(SkillCategory.createCustom("Backend", null, user));
        Skill categorizedSkill = skillRepository.save(
                Skill.create(user, "Spring", category, SkillStatus.LEARNING, 3, 0)
        );
        Skill uncategorizedSkill = skillRepository.save(
                Skill.create(user, "Docker", null, SkillStatus.LEARNING, 2, 1)
        );

        // 스킬이 연결된 학습 기록
        StudyLog studyLog = StudyLog.create(user, LocalDate.now(), "Spring 트랜잭션 학습", "내용");
        studyLog.updateSkills(Set.of(categorizedSkill));
        studyLogRepository.save(studyLog);

        // 로드맵: 스킬 연결된 스텝 + 미연결 스텝
        Roadmap roadmap = roadmapRepository.save(Roadmap.create(user, "백엔드 로드맵", "설명", null));
        roadmapStepRepository.save(RoadmapStep.create(roadmap, uncategorizedSkill, "Docker 학습", "설명", null, null, 0));
        roadmapStepRepository.save(RoadmapStep.create(roadmap, null, "스킬 미지정 스텝", "설명", null, null, 1));
    }

    @Test
    void 카테고리와_스킬_연관관계가_있는_데이터도_LazyInitializationException_없이_로딩된다() {
        // given
        String idempotencyKey = "lazy-loading-key-" + UUID.randomUUID();

        // when
        var response = careerAnalysisService.triggerAndAccept(userId, idempotencyKey);

        // then — 예외 없이 COMPLETED까지 도달하는지 (예외가 나면 FAILED로 빠짐)
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            assertThat(capturingAiConfig.getCapturedContext().get()).isNotNull();
        });

        CareerAnalysisContext context = capturingAiConfig.getCapturedContext().get();

        // Skill.category — 카테고리 있는 것/없는 것 둘 다 정상 반영
        assertThat(context.skills())
                .extracting(CareerAnalysisContext.SkillSummary::name, CareerAnalysisContext.SkillSummary::categoryName)
                .containsExactlyInAnyOrder(
                        org.assertj.core.groups.Tuple.tuple("Spring", "Backend"),
                        org.assertj.core.groups.Tuple.tuple("Docker", null)
                );

        // StudyLog.skills — 다대다 컬렉션이 실제로 로딩됨
        assertThat(context.studyLogs())
                .anySatisfy(log -> assertThat(log.relatedSkillNames()).containsExactly("Spring"));

        // RoadmapStep.skill — 연결된 것/안 된 것 둘 다 정상 반영
        assertThat(context.roadmaps())
                .flatExtracting(CareerAnalysisContext.RoadmapSummary::steps)
                .extracting(CareerAnalysisContext.RoadmapSummary.RoadmapStepSummary::relatedSkillName)
                .containsExactlyInAnyOrder("Docker", null);
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class CapturingAiConfig {

        private final AtomicReference<CareerAnalysisContext> capturedContext = new AtomicReference<>();

        @Bean
        @Primary
        AiCareerGapAnalysisClient capturingAiCareerGapAnalysisClient() {
            return context -> {
                capturedContext.set(context);
                return new CareerGapAnalysisResult("test", List.of(), List.of(), List.of());
            };
        }

        AtomicReference<CareerAnalysisContext> getCapturedContext() {
            return capturedContext;
        }
    }
}