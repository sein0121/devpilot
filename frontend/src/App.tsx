// src/App.tsx
import { useState } from 'react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { Login } from './pages/Login';
import { OAuthCallback } from './pages/OAuthCallback';
import { Dashboard } from './pages/Dashboard';
import type { UserResponse } from './api/types'; 

const queryClient = new QueryClient();

function AppContent() {
  const [user, setUser] = useState<UserResponse | null>(null);
  const isCallback = window.location.pathname === '/oauth/callback';

  if (user) return <Dashboard />;
  if (isCallback) return <OAuthCallback onSuccess={setUser} />;
  return <Login />;
}

export function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <AppContent />
    </QueryClientProvider>
  );
}