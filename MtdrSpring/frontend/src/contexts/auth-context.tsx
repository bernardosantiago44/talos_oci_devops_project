import React, { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import { apiClient } from '@/api/client';
import { queryClient } from '@/api/query-client';
import {
  login as loginRequest,
  me,
  signup as signupRequest,
  updateMe,
  type LoginRequest,
  type SignupRequest,
  type UpdateProfileRequest,
  type UserProfileResponse,
} from '@/api/generated';
import { readData } from '@/hooks/api/request';
import { getStoredAuthToken, removeStoredAuthToken, setStoredAuthToken } from '@/auth/auth-storage';

export type AuthUser = UserProfileResponse;

type AuthContextValue = {
  user: AuthUser | null;
  token: string | null;
  loading: boolean;
  isAuthenticated: boolean;
  login: (request: LoginRequest) => Promise<void>;
  signup: (request: SignupRequest) => Promise<void>;
  logout: () => void;
  loadCurrentUser: () => Promise<void>;
  updateProfile: (request: UpdateProfileRequest) => Promise<AuthUser>;
};

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [token, setToken] = useState<string | null>(() => getStoredAuthToken());
  const [user, setUser] = useState<AuthUser | null>(null);
  const [loading, setLoading] = useState(true);

  const clearAuthState = useCallback(() => {
    removeStoredAuthToken();
    setToken(null);
    setUser(null);
    queryClient.clear();
  }, []);

  const storeAuthResponse = useCallback((authToken?: string, authUser?: AuthUser) => {
    if (!authToken || !authUser?.userId) {
      throw new Error('Authentication response was incomplete.');
    }

    setStoredAuthToken(authToken);
    setToken(authToken);
    setUser(authUser);
  }, []);

  const loadCurrentUser = useCallback(async () => {
    const storedToken = getStoredAuthToken();
    if (!storedToken) {
      setLoading(false);
      setUser(null);
      return;
    }

    setLoading(true);
    try {
      const currentUser = await readData(me({ client: apiClient, throwOnError: true }));
      setToken(storedToken);
      setUser(currentUser);
    } catch {
      clearAuthState();
    } finally {
      setLoading(false);
    }
  }, [clearAuthState]);

  useEffect(() => {
    void loadCurrentUser();
  }, [loadCurrentUser]);

  const login = useCallback(
    async (request: LoginRequest) => {
      const response = await readData(loginRequest({ client: apiClient, body: request, throwOnError: true }));
      storeAuthResponse(response.token, response.user);
    },
    [storeAuthResponse]
  );

  const signup = useCallback(
    async (request: SignupRequest) => {
      const response = await readData(signupRequest({ client: apiClient, body: request, throwOnError: true }));
      storeAuthResponse(response.token, response.user);
    },
    [storeAuthResponse]
  );

  const updateProfile = useCallback(async (request: UpdateProfileRequest) => {
    const updatedUser = await readData(updateMe({ client: apiClient, body: request, throwOnError: true }));
    setUser(updatedUser);
    return updatedUser;
  }, []);

  const value = useMemo<AuthContextValue>(
    () => ({
      user,
      token,
      loading,
      isAuthenticated: Boolean(token && user?.userId),
      login,
      signup,
      logout: clearAuthState,
      loadCurrentUser,
      updateProfile,
    }),
    [clearAuthState, loadCurrentUser, loading, login, signup, token, updateProfile, user]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within AuthProvider.');
  }

  return context;
}
