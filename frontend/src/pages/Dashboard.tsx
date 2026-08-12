import { useQuery } from '@tanstack/react-query';
import { api } from '../api/client';
import type { UserResponse } from '../api/types'; 

export function Dashboard() {
  const { data, isLoading, error } = useQuery({
    queryKey: ['me'],
    queryFn: () => api.get<UserResponse>('/api/users/me'),
  });

  if (isLoading) return <p>불러오는 중...</p>;
  if (error) return <p>에러: {(error as Error).message}</p>;

  return (
    <div style={{ maxWidth: 480, margin: '4rem auto' }}>
      <h2>안녕하세요, {data?.nickname}님</h2>
      <ul>
        <li>이메일: {data?.email}</li>
        <li>Provider: {data?.provider}</li>
        <li>Role: {data?.role}</li>
      </ul>
      <button onClick={() => api.post('/api/auth/logout').then(() => { window.location.href = '/'; })}>
        로그아웃
      </button>
    </div>
  );
}