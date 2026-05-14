const ALLOWED_WORK_ITEM_LINK_TYPES = [
    'blocks',
    'is_blocked_by',
    'relates_to',
    'duplicates',
    'is_duplicated_by',
    'depends_on',
    'is_dependency_of',
];
const ALLOWED_RELATED_WORK_ITEM_TYPES = [
    'feature',
    'issue',
    'bug',
    'task',
];
function isWorkItemLinkType(value) {
    return ALLOWED_WORK_ITEM_LINK_TYPES.includes(value);
}
function isRelatedWorkItemType(value) {
    return ALLOWED_RELATED_WORK_ITEM_TYPES.includes(value);
}
function mapRelatedWorkItemSummary(params) {
    if (!isRelatedWorkItemType(params.type)) {
        throw new Error(`Unsupported related work item type: ${params.type}`);
    }
    return {
        id: params.id,
        title: params.title,
        type: params.type,
        status: params.status,
        priority: params.priority,
    };
}
export function mapWorkItemLinkDtoToModel(dto) {
    if (!isWorkItemLinkType(dto.type)) {
        throw new Error(`Unsupported work item link type: ${dto.type}`);
    }
    return {
        id: dto.linkId,
        type: dto.type,
        fromWorkItem: mapRelatedWorkItemSummary({
            id: dto.fromWorkItemId,
            title: dto.fromWorkItemTitle,
            type: dto.fromWorkItemType,
            status: dto.fromWorkItemStatus,
            priority: dto.fromWorkItemPriority,
        }),
        toWorkItem: mapRelatedWorkItemSummary({
            id: dto.toWorkItemId,
            title: dto.toWorkItemTitle,
            type: dto.toWorkItemType,
            status: dto.toWorkItemStatus,
            priority: dto.toWorkItemPriority,
        }),
        createdAt: dto.createdAt,
        createdByUserId: dto.createdByUserId,
    };
}
export function mapWorkItemLinkDtosToModels(dtos) {
    return dtos.map(mapWorkItemLinkDtoToModel);
}
