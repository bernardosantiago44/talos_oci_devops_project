import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom';
import { ProtectedRoute, PublicOnlyRoute } from '@/auth/auth-routes';
import { ThemeProvider } from '@/contexts/theme-context';
import { AuthProvider } from '@/contexts/auth-context';
import { LoginPage } from '@/features/auth/pages/login-page';
import { ProfilePage } from '@/features/auth/pages/profile-page';
import { SignupPage } from '@/features/auth/pages/signup-page';
import { WorkItemDashboardPage } from '@/features/work-items/pages/work-item-dashboard-page';

function App() {
  return (
    <ThemeProvider>
      <AuthProvider>
        <BrowserRouter>
          <Routes>
            <Route element={<PublicOnlyRoute />}>
              <Route path="/login" element={<LoginPage />} />
              <Route path="/signup" element={<SignupPage />} />
            </Route>

            <Route element={<ProtectedRoute />}>
              <Route path="/" element={<WorkItemDashboardPage />} />
              <Route path="/profile" element={<ProfilePage />} />
            </Route>

            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </BrowserRouter>
      </AuthProvider>
    </ThemeProvider>
  );
}

export default App;
