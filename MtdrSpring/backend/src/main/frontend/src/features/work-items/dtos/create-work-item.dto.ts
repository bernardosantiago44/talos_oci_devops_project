import type { WorkItemPriority } from '../enums/work-item-priority.enum';
import type { WorkItemStatus } from '../enums/work-item-status.enum';
import type { WorkItemType } from '../enums/work-item-type.enum';
import type { BugSeverity } from '../enums/bug-severity.enum';

export type CreateWorkItemDto = {
    sprintId?: string;
    title: string;
    description?: string;
    type: WorkItemType;
    status?: WorkItemStatus;
    priority: WorkItemPriority;
    externalLink?: string;
    estimatedMinutes?: number;
    dueDate?: string;
    assigneeUserIds?: string[];
    tagIds?: string[];
    featureDetails?: {
        businessValue?: string;
        acceptanceCriteria?: string;
    };
    issueDetails?: {
        environment?: string;
        reproductionSteps?: string;
    };
    bugDetails?: {
        severity?: BugSeverity;
        environment?: string;
        isReproducible?: boolean;
        steps?: string;
    };
};