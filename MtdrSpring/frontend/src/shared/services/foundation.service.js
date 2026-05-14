import { apiClient } from './api-client';
import { mockTags } from '../mock/tags.mock';
import { mockTeams } from '../mock/teams.mock';
export const foundationService = {
    /** Fetch real users from backend /appusers endpoint */
    async getUsers() {
        const result = await apiClient.get('/appusers');
        if (!result.success) {
            return { success: false, data: [], message: result.message };
        }
        // Backend already returns camelCase thanks to SQL aliases
        return { success: true, data: result.data };
    },
    /** Fetch real sprints from backend /sprints endpoint */
    async getSprints() {
        const result = await apiClient.get('/sprints');
        if (!result.success) {
            return { success: false, data: [], message: result.message };
        }
        return { success: true, data: result.data };
    },
    /** Tags are not stored in the backend yet — still local */
    async getTags() {
        return { success: true, data: structuredClone(mockTags) };
    },
    /** Teams are not stored in the backend yet — still local */
    async getTeams() {
        return { success: true, data: structuredClone(mockTeams) };
    },
    async getUserOptions() {
        const usersResult = await this.getUsers();
        if (!usersResult.success) {
            return { success: false, data: [], message: usersResult.message };
        }
        const options = usersResult.data.map((user) => ({
            value: user.userId,
            label: user.name,
        }));
        return { success: true, data: options };
    },
    async getTagOptions() {
        const options = mockTags.map((tag) => ({
            value: tag.id,
            label: tag.name,
        }));
        return { success: true, data: options };
    },
    async getTeamOptions() {
        const options = mockTeams.map((team) => ({
            value: team.id,
            label: team.name,
        }));
        return { success: true, data: options };
    },
};
