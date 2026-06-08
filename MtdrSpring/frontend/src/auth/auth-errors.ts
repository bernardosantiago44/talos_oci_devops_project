type ApiError = {
  message?: string;
  error?: string;
};

export function getAuthErrorMessage(error: unknown, fallback: string): string {
  if (typeof error === 'string' && error.trim()) {
    return error;
  }

  if (error && typeof error === 'object') {
    const apiError = error as ApiError;
    return apiError.message ?? apiError.error ?? fallback;
  }

  return fallback;
}
