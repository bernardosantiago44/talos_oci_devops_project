export const apiQueryKeys = {
  todos: {
    all: ['todos'] as const,
    list: () => [...apiQueryKeys.todos.all, 'list'] as const,
    detail: (id: number | string | undefined) => [...apiQueryKeys.todos.all, 'detail', id] as const,
  },
  workItems: {
    all: ['workItems'] as const,
    list: () => [...apiQueryKeys.workItems.all, 'list'] as const,
    detail: (id: string | undefined) => [...apiQueryKeys.workItems.all, 'detail', id] as const,
    assignees: (id: string | undefined) => [...apiQueryKeys.workItems.detail(id), 'assignees'] as const,
    byTelegramUser: (telegramUserId: string | undefined) =>
      [...apiQueryKeys.workItems.all, 'telegramUser', telegramUserId] as const,
  },
  sprints: {
    all: ['sprints'] as const,
    list: () => [...apiQueryKeys.sprints.all, 'list'] as const,
    detail: (id: string | undefined) => [...apiQueryKeys.sprints.all, 'detail', id] as const,
  },
  appUsers: {
    all: ['appUsers'] as const,
    list: () => [...apiQueryKeys.appUsers.all, 'list'] as const,
  },
  analytics: {
    all: ['analytics'] as const,
    velocity: () => [...apiQueryKeys.analytics.all, 'velocity'] as const,
    debug: () => [...apiQueryKeys.analytics.all, 'debug'] as const,
    dashboard: () => [...apiQueryKeys.analytics.all, 'dashboard'] as const,
  },
} as const;
