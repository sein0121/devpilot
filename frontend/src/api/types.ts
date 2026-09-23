export interface UserResponse {
    id: number;
    email: string;
    nickname: string;
    provider: string;
    role: string;
    createdAt: string;
    updatedAt: string;
}

export interface GithubRepositoryItem {
    name: string;
    description: string | null;
    language: string | null;
    stars: number;
    isFork: boolean;
  }
  
export interface GithubContributionItem {
  date: string;
  count: number;
}
  
export interface GithubAccountResponse {
  githubUsername: string;
  avatarUrl: string | null;
  publicRepoCount: number;
  followerCount: number;
  followingCount: number;
  lastSyncedAt: string;
  repositories: GithubRepositoryItem[];
  contributions: GithubContributionItem[];
}

export type SkillStatus = 'LEARNING' | 'PROFICIENT';

export interface SkillCategoryItem {
  id: number;
  name: string;
  parentId: number | null;
}
  
export interface SkillItem {
  id: number;
  name: string;
  categoryName: string | null;
  status: SkillStatus;
  proficiency: number;
  displayOrder: number;
}

export interface StudyLogItem {
  id: number;
  date: string; // "2026-08-20"
  title: string;
  content: string;
  skillNames: string[];
}

export type RoadmapStepStatus = 'TODO' | 'IN_PROGRESS' | 'DONE';

export interface RoadmapSummary {
  id: number;
  title: string;
  targetDate: string | null;
  totalSteps: number;
  doneSteps: number;
  progress: number;
}

export interface RoadmapStepItem {
  id: number;
  title: string;
  description: string | null;
  status: RoadmapStepStatus;
  targetDate: string | null;
  link: string | null;
  skillName: string | null;
  displayOrder: number;
}

export interface RoadmapLinkItem {
  id: number;
  url: string;
  label: string | null;
}

export interface RoadmapDetail {
  id: number;
  title: string;
  description: string | null;
  targetDate: string | null;
  progress: number;
  steps: RoadmapStepItem[];
  links: RoadmapLinkItem[];
}

export interface AiRoadmapStepDraft {
  title: string;
  description: string | null;
  suggestedSkillName: string;
  matchedSkillId: number | null;
}

export interface AiRoadmapDraftResponse {
  steps: AiRoadmapStepDraft[];
}

export type CareerAnalysisStatus = 'PENDING' | 'RUNNING' | 'COMPLETED' | 'FAILED';

export type CareerGapPriority = 'HIGH' | 'MEDIUM' | 'LOW';

export interface CareerGapStrength {
  title: string;
  evidence: string;
}

export interface CareerGapItem {
  skill: string;
  reason: string;
  priority: CareerGapPriority;
}

export interface CareerGapRecommendation {
  title: string;
  description: string;
  relatedSkill: string;
  priority: CareerGapPriority;
}

export interface CareerGapAnalysisResult {
  summary: string;
  strengths: CareerGapStrength[];
  gaps: CareerGapItem[];
  recommendations: CareerGapRecommendation[];
}

export interface CareerAnalysisResponse {
  id: number;
  status: CareerAnalysisStatus;
  result: string | null; // JSON 문자열 — 프론트에서 JSON.parse 필요
  errorMessage: string | null;
  startedAt: string | null;
  completedAt: string | null;
}