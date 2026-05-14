import { apiClient } from './api-client';
export const analyticsService = {
    async getVelocity() {
        return apiClient.get('/analytics/velocity');
    },
    async getDashboard() {
        return apiClient.get('/analytics/dashboard');
    },
};
