const AUTH_TOKEN_KEY = 'talos.auth.token';

export function getStoredAuthToken(): string | null {
  if (typeof window === 'undefined') return null;
  return window.localStorage.getItem(AUTH_TOKEN_KEY);
}

export function setStoredAuthToken(token: string): void {
  window.localStorage.setItem(AUTH_TOKEN_KEY, token);
}

export function removeStoredAuthToken(): void {
  window.localStorage.removeItem(AUTH_TOKEN_KEY);
}
