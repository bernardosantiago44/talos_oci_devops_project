export const apiQueryKeys = {
    todos: {
        all: ['todos'],
        list: () => [...apiQueryKeys.todos.all, 'list'],
        detail: (id) => [...apiQueryKeys.todos.all, 'detail', id],
    },
    workItems: {
        all: ['workItems'],
        list: () => [...apiQueryKeys.workItems.all, 'list'],
        detail: (id) => [...apiQueryKeys.workItems.all, 'detail', id],
        assignees: (id) => [...apiQueryKeys.workItems.detail(id), 'assignees'],
        byTelegramUser: (telegramUserId) => [...apiQueryKeys.workItems.all, 'telegramUser', telegramUserId],
    },
    sprints: {
        all: ['sprints'],
        list: () => [...apiQueryKeys.sprints.all, 'list'],
        detail: (id) => [...apiQueryKeys.sprints.all, 'detail', id],
    },
    appUsers: {
        all: ['appUsers'],
        list: () => [...apiQueryKeys.appUsers.all, 'list'],
    },
    tags: {
        all: ['tags'],
        list: () => [...apiQueryKeys.tags.all, 'list'],
        detail: (id) => [...apiQueryKeys.tags.all, 'detail', id],
    },
    analytics: {
        all: ['analytics'],
        velocity: () => [...apiQueryKeys.analytics.all, 'velocity'],
        debug: () => [...apiQueryKeys.analytics.all, 'debug'],
        dashboard: () => [...apiQueryKeys.analytics.all, 'dashboard'],
    },
};
