const BASE_URL = import.meta.env.VITE_API_BASE_URL;

if (!BASE_URL) {
  throw new Error('VITE_API_BASE_URL이 설정되지 않았습니다. .env 파일을 확인하세요.');
}

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
    credentials: 'include', // 세션 쿠키 전송 필수
    headers: {
      'Content-Type': 'application/json',
      ...options?.headers,
    },
  });

  const body = await res.json();

  if (!res.ok) {
    throw new ApiError(res.status, body.message ?? 'Unknown error');
  }

  return body.data as T;
}

export const api = {
  get: <T>(path: string) => request<T>(path),
  post: <T>(path: string, body?: unknown) =>
    request<T>(path, { method: 'POST', body: body ? JSON.stringify(body) : undefined }),
};

export function loginWithGithub() {
  window.location.href = `${BASE_URL}/oauth2/authorization/github`;
}