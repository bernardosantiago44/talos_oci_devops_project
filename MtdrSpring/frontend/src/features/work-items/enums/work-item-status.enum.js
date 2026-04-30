export const WORK_ITEM_STATUSES = [
    'TODO',
    'NEW',
    'IN_PROGRESS',
    'BLOCKED',
    'DONE'
];
/** Backend stores 'NEW'; the UI treats it as 'TODO'. */
export function normalizeStatus(raw) {
    if (!raw)
        return 'TODO';
    const upper = raw.toUpperCase();
    if (upper === 'NEW')
        return 'TODO';
    if (WORK_ITEM_STATUSES.includes(upper))
        return upper;
    return 'TODO';
}
/** Convert frontend status back to backend format for API calls. */
export function toBackendStatus(status) {
    return status === 'TODO' ? 'NEW' : status;
}
