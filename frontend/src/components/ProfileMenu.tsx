import { useState, useRef, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useQueryClient } from '@tanstack/react-query';
import { api } from '../api/client';
import type { UserResponse, GithubAccountResponse } from '../api/types';

export function ProfileMenu({
  user,
  avatarUrl,
}: {
  user: UserResponse;
  avatarUrl?: string | null;
}) {
  const [open, setOpen] = useState(false);
  const ref = useRef<HTMLDivElement>(null);
  const navigate = useNavigate();
  const queryClient = useQueryClient();

  useEffect(() => {
    function handleClickOutside(e: MouseEvent) {
      if (ref.current && !ref.current.contains(e.target as Node)) {
        setOpen(false);
      }
    }
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  return (
    <div className="profile-menu" ref={ref}>
      <button className="avatar-btn" onClick={() => setOpen(!open)}>
        {avatarUrl ? (
          <img src={avatarUrl} alt={user.nickname} className="avatar-img" />
        ) : (
          <span className="avatar-fallback">{user.nickname.charAt(0).toUpperCase()}</span>
        )}
      </button>

      {open && (
        <div className="profile-dropdown">
          <div className="profile-dropdown-header">
            <div className="profile-dropdown-email">{user.email}</div>
            <div className="profile-dropdown-meta">{user.provider} · {user.role}</div>
          </div>
          <button
            className="profile-dropdown-logout"
            onClick={() =>
              api.post('/api/auth/logout').then(() => {
                queryClient.removeQueries({ queryKey: ['session'] });
                queryClient.removeQueries({ queryKey: ['me'] });
                queryClient.removeQueries({ queryKey: ['github'] });
                navigate('/', { replace: true });
              })
            }
          >
            로그아웃
          </button>
        </div>
      )}
    </div>
  );
}