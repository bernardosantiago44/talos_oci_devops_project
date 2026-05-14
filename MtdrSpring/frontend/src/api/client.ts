import { client } from './generated/client.gen';

const apiBaseUrl = process.env.REACT_APP_API_BASE_URL ?? '';

client.setConfig({
  baseUrl: apiBaseUrl,
  throwOnError: true,
});

export const apiClient = client;
