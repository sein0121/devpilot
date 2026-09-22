import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { Login } from './pages/Login';
import { OAuthCallback } from './pages/OAuthCallback';
import { HomePage } from './pages/HomePage';
import { GithubPage } from './pages/GithubPage';
import { SkillsPage } from './pages/SkillsPage';
import { StudyLogPage } from './pages/StudyLogPage';
import { RoadmapPage } from './pages/RoadmapPage';
import { ProtectedRoute } from './routes/ProtectedRoute';
import { AppLayout } from './layouts/AppLayout';
import { CareerAnalysisPage } from './pages/CareerAnalysisPage';

const queryClient = new QueryClient();

export function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<Login />} />
          <Route path="/oauth/callback" element={<OAuthCallback />} />

          <Route
            element={
              <ProtectedRoute>
                <AppLayout />
              </ProtectedRoute>
            }
          >
            <Route path="/dashboard" element={<HomePage />} />
            <Route path="/github" element={<GithubPage />} />
            <Route path="/skills" element={<SkillsPage />} />
            <Route path="/study-log" element={<StudyLogPage />} />
            <Route path="/roadmap" element={<RoadmapPage />} />
            <Route path="/career-analysis" element={<CareerAnalysisPage />} />
          </Route>

          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </BrowserRouter>
    </QueryClientProvider>
  );
}
