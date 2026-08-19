// src/pages/Login.tsx
import { loginWithGithub } from '../api/client';

export function Login() {
  return (
    <div className="login-wrap">
      <div className="login-title">DevPilot</div>
      <div className="login-subtitle">GitHub 활동으로 성장하는 커리어 코칭</div>
      <button className="btn-github" onClick={loginWithGithub}>
        GitHub로 로그인
      </button>
    </div>
  );
}