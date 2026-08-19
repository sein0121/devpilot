import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useQueryClient } from '@tanstack/react-query';
import { api } from '../api/client';

export function OAuthCallback() {
  const navigate = useNavigate();
  const queryClient = useQueryClient();

  useEffect(() => {
    api.get('/api/auth/me')
      .then(() => {
        queryClient.invalidateQueries({ queryKey: ['session'] });
        navigate('/dashboard', { replace: true });
      })
      .catch((err) => {
        console.error('로그인 확인 실패', err);
        alert('로그인에 실패했습니다.');
        navigate('/', { replace: true });
      });
  }, [navigate, queryClient]);

  return <p style={{ textAlign: 'center', marginTop: '4rem' }}>로그인 처리 중...</p>;
}