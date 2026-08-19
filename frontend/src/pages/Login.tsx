import { Navigate } from 'react-router-dom';
import { loginWithGithub } from '../api/client';
import { useSession } from '../hooks/useSession';

export function Login() {
  const { data, isLoading } = useSession();

  if (isLoading) return <div className="page">확인 중...</div>;
  if (data) return <Navigate to="/dashboard" replace />;

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