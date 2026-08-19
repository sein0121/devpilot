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
    publicRepoCount: number;
    followerCount: number;
    followingCount: number;
    lastSyncedAt: string;
    repositories: GithubRepositoryItem[];
    contributions: GithubContributionItem[];
  }