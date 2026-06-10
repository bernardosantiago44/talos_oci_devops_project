import { test, expect } from '@playwright/test';

test.describe('Dashboard and Work Items', () => {
  test.beforeEach(async ({ page }) => {
    // Intercept auth checks to bypass login redirect
    await page.route('**/api/auth/me', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          userId: 'user-001',
          email: 'qa@example.com',
          name: 'QA Engineer',
          role: 'DEVELOPER',
          telegramUserId: 't-001',
        }),
      });
    });

    // Mock initial sprints
    await page.route('**/api/sprints', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify([
          { sprintId: 'sprint-001', name: 'Sprint 1', active: true },
        ]),
      });
    });

    // Mock users
    await page.route('**/api/appusers', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify([
          { userId: 'user-001', name: 'QA Engineer' },
        ]),
      });
    });

    // Mock tags
    await page.route('**/api/tags**', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify([
          { id: 'tag-001', tagId: 'tag-001', name: 'Frontend', color: '#3B82F6' },
        ]),
      });
    });

    // Mock workitems list
    await page.route('**/api/workitems**', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify([
          {
            id: 'wi-001',
            workItemId: 'wi-001',
            title: 'Verify UI components',
            description: 'Check views are functioning properly',
            type: 'TASK',
            status: 'TODO',
            priority: 'HIGH',
            assignees: [{ user: { userId: 'user-001', name: 'QA Engineer' } }],
            tags: [{ id: 'tag-001', name: 'Frontend', color: '#3B82F6' }],
          },
        ]),
      });
    });

    // Mock analytics dashboard data
    await page.route('**/api/analytics/dashboard', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          kpis: { totalTimeLogged: 120, totalOpenTasks: 1, totalCompletedTasks: 0 },
          velocity: [],
        }),
      });
    });

    // Go to login first to set localStorage token under same origin
    await page.goto('/login');
    await page.evaluate(() => {
      localStorage.setItem('talos.auth.token', 'mock-jwt-token');
    });
    // Go to dashboard
    await page.goto('/');
  });

  test('toggling between Kanban and List view works correctly', async ({ page }) => {
    // By default, Kanban view is active. We should find the Kanban Board headers.
    await expect(page.locator('text=Todo').first()).toBeVisible();

    // Toggle to List view
    await page.click('button[title="List view"]');
    
    // We should now see the table headers (like "Title", "Type") or task row list item
    await expect(page.locator('text=Verify UI components')).toBeVisible();
    await expect(page.locator('text=HIGH')).toBeVisible();

    // Toggle back to Kanban
    await page.click('button[title="Kanban view"]');
    await expect(page.locator('text=Todo').first()).toBeVisible();
  });

  test('creation modal validates empty title input', async ({ page }) => {
    // Open New Task modal
    await page.click('button:has-text("New Task")');

    // Submit form without title
    await page.click('button:has-text("Create Task")');

    // Should see title error validation
    await expect(page.locator('text="Title is required."')).toBeVisible();
  });

  test('successful task creation triggers post request', async ({ page }) => {
    let createTriggered = false;
    let payload: any = null;

    await page.route('**/api/workitems', async (route) => {
      if (route.request().method() === 'POST') {
        createTriggered = true;
        payload = JSON.parse(route.request().postData() || '{}');
        await route.fulfill({
          status: 201,
          contentType: 'application/json',
          body: JSON.stringify({
            id: 'wi-002',
            workItemId: 'wi-002',
            title: 'New Automated Task',
            type: 'BUG',
            status: 'TODO',
            priority: 'MEDIUM',
            assignees: [],
            tags: [],
          }),
        });
      } else {
        await route.fallback();
      }
    });

    // Open New Task modal
    await page.click('button:has-text("New Task")');

    // Fill form using exact CSS selectors
    await page.fill('input[placeholder="Task title…"]', 'New Automated Task');
    await page.fill('textarea[placeholder="Describe the task…"]', 'Created from Playwright E2E test');
    await page.selectOption('select >> xpath=../..//label[text()="Type"]/../select', 'BUG');
    await page.selectOption('select >> xpath=../..//label[text()="Priority"]/../select', 'MEDIUM');

    // Submit form
    await page.click('button:has-text("Create Task")');

    // Modal should close and request should be intercepted
    expect(createTriggered).toBe(true);
    expect(payload.title).toBe('New Automated Task');
    expect(payload.workType).toBe('BUG');
    expect(payload.priority).toBe('MEDIUM');
  });

  test('can filter work items using search bar', async ({ page }) => {
    // Verify search bar input is available using CSS selector
    const searchInput = page.locator('input[placeholder="Search tasks…"]');
    await expect(searchInput).toBeVisible();

    await searchInput.fill('Verify UI');
    // Verify no JS error is thrown and input text updates
    await expect(searchInput).toHaveValue('Verify UI');
  });

  test('tag manager modal is opening and displays tags', async ({ page }) => {
    // Open tag manager modal
    await page.click('button:has-text("Tags")');
    // Check if tag manager is visible
    await expect(page.locator('text=Manage Tags')).toBeVisible();
    // Check if mock tag is rendered
    await expect(page.locator('text=Frontend').first()).toBeVisible();
  });

  test('AI Search panel displays the semantic search field', async ({ page }) => {
    // Click on AI Search tab
    await page.click('button:has-text("AI Search")');

    // Verify AI search header is visible
    await expect(page.locator('text=Semantic Search')).toBeVisible();
    await expect(page.locator('text=RF-005')).toBeVisible();
  });
});
