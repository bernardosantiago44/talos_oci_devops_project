import type { ApiResult } from '../dtos/api-result.dto';
import { apiClient } from './api-client';

export interface SprintVelocity {
    sprintId: string;
    sprintName: string;
    sprintStatus: string;
    startDate?: string;
    endDate?: string;
    totalTasks: number;
    completedTasks: number;
    fulfillmentPct: number;
}

export interface VelocityData {
    target: number;
    overallPct: number;
    sprints: SprintVelocity[];
}

export interface DeveloperRow {
    DEVELOPER: string;
    SPRINT_NAME: string;
    TASKS_COMPLETED: number;
    REAL_HOURS: number;
}

export interface DashboardKpis {
    totalTasks: number;
    totalHours: number;
    avgTasksPerDev: number;
    avgHoursPerDev: number;
}

export interface DashboardData {
    kpis: DashboardKpis;
    chartData: DeveloperRow[];
}

export const analyticsService = {
    async getVelocity(): Promise<ApiResult<VelocityData>> {
        return apiClient.get<VelocityData>('/analytics/velocity');
    },
    async getDashboard(): Promise<ApiResult<DashboardData>> {
        return apiClient.get<DashboardData>('/analytics/dashboard');
    },
};
