import { Navigate } from 'react-router-dom';
import { useSession } from '../hooks/useSession';

export function ProtectedRoute({ children }: { children: React.ReactNode }) {
  const { data, isLoading, isError } = useSession();

  if (isLoading) return <div className="page">확인 중...</div>;
  if (isError || !data) return <Navigate to="/" replace />;

  return <>{children}</>;
}