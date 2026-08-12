import { useEffect } from 'react';
import { api } from '../api/client';
import type { UserResponse } from '../api/types'; 

export function OAuthCallback({ onSuccess }: { onSuccess: (user: UserResponse) => void }) {
  useEffect(() => {
    api.get<UserResponse>('/api/auth/me')
      .then((user) => {
        window.history.replaceState(null, '', '/'); // 주소창 정리
        onSuccess(user);
      })
      .catch((err) => {
        console.error('로그인 확인 실패', err);
        alert('로그인에 실패했습니다.');
      });
  }, [onSuccess]);

  return <p style={{ textAlign: 'center', marginTop: '4rem' }}>로그인 처리 중...</p>;
}