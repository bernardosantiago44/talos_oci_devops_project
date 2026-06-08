import { client } from './generated/client.gen';
import { getStoredAuthToken } from '@/auth/auth-storage';

const apiBaseUrl = process.env.REACT_APP_API_BASE_URL ?? '';

client.setConfig({
  baseUrl: apiBaseUrl,
  throwOnError: true,
  auth: () => getStoredAuthToken() ?? undefined,
});

export const apiClient = client;
