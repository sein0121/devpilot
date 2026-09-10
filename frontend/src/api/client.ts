// src/api/client.ts
// const BASE_URL = import.meta.env.VITE_API_BASE_URL;

// if (!BASE_URL) {
//   throw new Error('VITE_API_BASE_URL이 설정되지 않았습니다. .env 파일을 확인하세요.');
// }

const BASE_URL = import.meta.env.VITE_API_BASE_URL ?? "";

export class ApiError extends Error {
  status: number;
  constructor(status: number, message: string) {
    super(message);
    this.status = status;
  }
}

async function request<T>(path: string, options?: RequestInit): Promise<T> {
  const res = await fetch(`${BASE_URL}${path}`, {
    ...options,
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json',
      ...options?.headers,
    },
  });

  // 204 No Content 이거나 body가 비어있으면 JSON 파싱을 시도하지 않음
  if (res.status === 204) {
    if (!res.ok) {
      throw new ApiError(res.status, '요청 처리에 실패했습니다.');
    }
    return undefined as T;
  }

  const text = await res.text();
  const body = text ? JSON.parse(text) : null;

  if (!res.ok) {
    throw new ApiError(res.status, body?.message ?? 'Unknown error');
  }

  return body?.data as T;
}

export const api = {
  get: <T>(path: string) => request<T>(path),
  post: <T>(path: string, body?: unknown) =>
    request<T>(path, { method: 'POST', body: body ? JSON.stringify(body) : undefined }),
  put: <T>(path: string, body?: unknown) =>
    request<T>(path, { method: 'PUT', body: body ? JSON.stringify(body) : undefined }),
  patch: <T>(path: string, body?: unknown) =>
    request<T>(path, { method: 'PATCH', body: body ? JSON.stringify(body) : undefined }),
  delete: <T>(path: string) =>
    request<T>(path, { method: 'DELETE' }),
};

export function loginWithGithub() {
  window.location.href = `${BASE_URL}/oauth2/authorization/github`;
}