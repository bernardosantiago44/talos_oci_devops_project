import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { useAuth } from '@/contexts/auth-context';

function FullPageLoader() {
  return (
    <div className="flex min-h-screen items-center justify-center bg-zinc-50 dark:bg-zinc-950">
      <div className="h-8 w-8 animate-spin rounded-full border-2 border-zinc-200 border-t-sky-500 dark:border-zinc-700 dark:border-t-sky-400" />
    </div>
  );
}

export function ProtectedRoute() {
  const auth = useAuth();
  const location = useLocation();

  if (auth.loading) {
    return <FullPageLoader />;
  }

  if (!auth.isAuthenticated) {
    return <Navigate to="/login" replace state={{ from: location }} />;
  }

  return <Outlet />;
}

export function PublicOnlyRoute() {
  const auth = useAuth();

  if (auth.loading) {
    return <FullPageLoader />;
  }

  if (auth.isAuthenticated) {
    return <Navigate to="/" replace />;
  }

  return <Outlet />;
}
