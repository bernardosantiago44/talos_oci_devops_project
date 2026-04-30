import { useQuery } from '@tanstack/react-query';
import { apiClient } from '@/api/client';
import { debug, getDashboardData, getVelocity } from '@/api/generated';
import { apiQueryKeys } from './query-keys';
import { readData } from './request';

export function useAnalyticsVelocity() {
  return useQuery({
    queryKey: apiQueryKeys.analytics.velocity(),
    queryFn: () => readData(getVelocity({ client: apiClient, throwOnError: true })),
  });
}

export function useAnalyticsDebug() {
  return useQuery({
    queryKey: apiQueryKeys.analytics.debug(),
    queryFn: () => readData(debug({ client: apiClient, throwOnError: true })),
  });
}

export function useAnalyticsDashboard() {
  return useQuery({
    queryKey: apiQueryKeys.analytics.dashboard(),
    queryFn: () => readData(getDashboardData({ client: apiClient, throwOnError: true })),
  });
}
