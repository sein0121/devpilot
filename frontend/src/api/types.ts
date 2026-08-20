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