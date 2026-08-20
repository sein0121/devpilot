import { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { api, ApiError } from '../api/client';
import type { UserResponse, GithubAccountResponse } from '../api/types';
import { ContributionGraph } from '../components/ContributionGraph';
import { useNavigate } from 'react-router-dom';
import { SkillSection } from '../components/SkillSection';
import { ProfileMenu } from '../components/ProfileMenu';
import { StudyLogSection } from '../components/StudyLogSection';

export function Dashboard() {
  const queryClient = useQueryClient();
  const [reposOpen, setReposOpen] = useState(false);
  const [commitsOpen, setCommitsOpen] = useState(false);

  const navigate = useNavigate();

  const { data: user, isLoading, error } = useQuery({
    queryKey: ['me'],
    queryFn: () => api.get<UserResponse>('/api/users/me'),
    retry: false,
  });

  const {
    data: github,
    isLoading: isGithubLoading,
    error: githubError,
  } = useQuery({
    queryKey: ['github', 'me'],
    queryFn: () => api.get<GithubAccountResponse>('/api/github/me'),
    retry: false,
  });

  const syncMutation = useMutation({
    mutationFn: () => api.post('/api/github/sync'),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['github', 'me'] }),
  });

  if (isLoading) return <div className="page">불러오는 중...</div>;
  if (error) return <div className="page">에러: {(error as Error).message}</div>;

  const githubNotSynced = githubError instanceof ApiError && githubError.status === 404;
  const totalCommits = github?.contributions.reduce((sum, c) => sum + c.count, 0) ?? 0;

  return (
    <div className="page">
      <div className="greeting-row">
      <div className="greeting">안녕하세요, {user?.nickname}님 👋</div>
        {user && <ProfileMenu user={user} avatarUrl={github?.avatarUrl} />}
      </div>

      <div className="card">
        <div className="card-header">
          <p className="card-title">GitHub</p>
          <button className="btn btn-primary" onClick={() => syncMutation.mutate()} disabled={syncMutation.isPending}>
            {syncMutation.isPending ? '동기화 중...' : '동기화'}
          </button>
        </div>

        {isGithubLoading && <div className="empty-state">불러오는 중...</div>}
        {githubNotSynced && <div className="empty-state">아직 동기화된 데이터가 없어요. 동기화 버튼을 눌러주세요.</div>}
        {githubError && !githubNotSynced && (
          <div className="empty-state">에러: {(githubError as Error).message}</div>
        )}

        {github && (
          <>
            <div className="stat-row">
              <div
                className={`stat-item clickable ${reposOpen ? 'open' : ''}`}
                onClick={() => setReposOpen(!reposOpen)}
              >
                <div className="stat-value">{github.publicRepoCount}</div>
                <div className="stat-label">
                  Repos <span className={`chevron ${reposOpen ? 'rotated' : ''}`}>▾</span>
                </div>
              </div>

              <div
                className={`stat-item clickable ${commitsOpen ? 'open' : ''}`}
                onClick={() => setCommitsOpen(!commitsOpen)}
              >
                <div className="stat-value">{totalCommits}</div>
                <div className="stat-label">
                  Commits <span className={`chevron ${commitsOpen ? 'rotated' : ''}`}>▾</span>
                </div>
              </div>

              <div className="stat-item">
                <div className="stat-value">{github.followerCount}</div>
                <div className="stat-label">Followers</div>
              </div>

              <div className="stat-item">
                <div className="stat-value">{github.followingCount}</div>
                <div className="stat-label">Following</div>
              </div>
            </div>

            {reposOpen && (
              <div className="dropdown-panel">
                <ul className="repo-list">
                  {github.repositories.map((repo) => (
                    <li key={repo.name} className="repo-item">
                      <div className="repo-name">{repo.name}</div>
                      {repo.description && <div className="repo-desc">{repo.description}</div>}
                      <div className="repo-tags">
                        {repo.language && <span className="tag">{repo.language}</span>}
                        <span className="tag">⭐ {repo.stars}</span>
                        {repo.isFork && <span className="tag">fork</span>}
                      </div>
                    </li>
                  ))}
                </ul>
              </div>
            )}

            {commitsOpen && (
              <div className="dropdown-panel">
                <ul className="commit-list">
                  {github.contributions.slice(-14).reverse().map((c) => (
                    <li key={c.date}>
                      <span>{c.date}</span>
                      <span>{c.count}회</span>
                    </li>
                  ))}
                </ul>
              </div>
            )}

            <ContributionGraph contributions={github.contributions} />

            <div className="sync-meta">
              @{github.githubUsername} · 마지막 동기화 {new Date(github.lastSyncedAt).toLocaleString()}
            </div>
          </>
        )}
      </div>
      <SkillSection />
      <StudyLogSection />
    </div>
  );
}