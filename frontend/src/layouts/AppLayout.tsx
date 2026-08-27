import { Outlet , useNavigate } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { api } from '../api/client';
import type { UserResponse, GithubAccountResponse } from '../api/types';
import { ProfileMenu } from '../components/ProfileMenu';
import { BottomNav } from '../components/BottomNav';

export function AppLayout() {
    const navigate = useNavigate();
  
    const { data: user } = useQuery({
      queryKey: ['me'],
      queryFn: () => api.get<UserResponse>('/api/users/me'),
    });
  
    const { data: github } = useQuery({
      queryKey: ['github', 'me'],
      queryFn: () => api.get<GithubAccountResponse>('/api/github/me'),
      retry: false,
    });
  
    return (
      <div className="app-layout">
        <div className="page app-content">
          <div className="greeting-row">
            <div className="greeting greeting-clickable" onClick={() => navigate('/dashboard')}>
              <span className="brand-mark">DP</span>
              <span className="greeting-text">
                {user ? `안녕하세요, ${user.nickname}님` : 'DevPilot'}
              </span>
            </div>
            {user && <ProfileMenu user={user} avatarUrl={github?.avatarUrl} />}
          </div>
  
          <Outlet />
        </div>
  
        <BottomNav />
      </div>
    );
  }
