export type WorkLogMode = 'log' | 'complete';

export type WorkLogDto = {
    workItemId: string;
    userId: string;
    minutes: number;
    note?: string;
};
