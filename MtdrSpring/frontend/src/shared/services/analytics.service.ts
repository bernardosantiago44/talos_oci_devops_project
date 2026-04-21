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

export const analyticsService = {
    async getVelocity(): Promise<ApiResult<VelocityData>> {
        return apiClient.get<VelocityData>('/analytics/velocity');
    },
};
