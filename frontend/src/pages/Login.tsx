import { loginWithGithub } from '../api/client';

export function Login() {
  return (
    <div style={{ display: 'flex', justifyContent: 'center', marginTop: '4rem' }}>
      <button onClick={loginWithGithub} style={{ padding: '0.75rem 1.5rem', fontSize: '1rem' }}>
        GitHub로 로그인
      </button>
    </div>
  );
}