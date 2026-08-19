import { useQuery } from '@tanstack/react-query';
import { api } from '../api/client';
import type { UserResponse } from '../api/types';

export function useSession() {
  return useQuery({
    queryKey: ['session'],
    queryFn: () => api.get<UserResponse>('/api/auth/me'),
    retry: false,
  });
}