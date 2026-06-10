import { test, expect } from '@playwright/test';

test.describe('Authentication Flows', () => {
  test('successful login redirects to dashboard', async ({ page }) => {
    // Intercept login api call
    await page.route('**/api/auth/login', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          token: 'mock-jwt-token',
          user: {
            userId: 'user-001',
            email: 'qa@example.com',
            name: 'QA Engineer',
            role: 'DEVELOPER',
            telegramUserId: 't-001',
          },
        }),
      });
    });

    // Intercept me api call (when loading dashboard)
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

    // Intercept other dashboard calls to prevent 404s
    await page.route('**/api/sprints', async (route) => {
      await route.fulfill({ status: 200, contentType: 'application/json', body: '[]' });
    });
    await page.route('**/api/appusers', async (route) => {
      await route.fulfill({ status: 200, contentType: 'application/json', body: '[]' });
    });
    await page.route('**/api/tags', async (route) => {
      await route.fulfill({ status: 200, contentType: 'application/json', body: '[]' });
    });
    await page.route('**/api/workitems**', async (route) => {
      await route.fulfill({ status: 200, contentType: 'application/json', body: '[]' });
    });
    await page.route('**/api/analytics/**', async (route) => {
      await route.fulfill({ status: 200, contentType: 'application/json', body: '{}' });
    });

    await page.goto('/login');
    await page.fill('input[type="email"]', 'qa@example.com');
    await page.fill('input[type="password"]', 'password123');
    await page.click('button[type="submit"]');

    // Should redirect to dashboard (/)
    await expect(page).toHaveURL('/');
  });

  test('failed login shows validation error message', async ({ page }) => {
    // Intercept login api call to return 401
    await page.route('**/api/auth/login', async (route) => {
      await route.fulfill({
        status: 401,
        contentType: 'application/json',
        body: JSON.stringify({
          error: 'UNAUTHORIZED',
          message: 'Invalid email or password',
        }),
      });
    });

    await page.goto('/login');
    await page.fill('input[type="email"]', 'invalid@example.com');
    await page.fill('input[type="password"]', 'wrongpass');
    await page.click('button[type="submit"]');

    // Should display the error message on screen
    const errorMsg = page.locator('.text-rose-600');
    await expect(errorMsg).toBeVisible();
    await expect(errorMsg).toContainText('Invalid email or password');
  });

  test('signup flow registers a new user', async ({ page }) => {
    // Intercept signup api call
    await page.route('**/api/auth/signup', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          token: 'mock-jwt-token-signup',
          user: {
            userId: 'user-002',
            email: 'newuser@example.com',
            name: 'New QA Tester',
            role: 'DEVELOPER',
          },
        }),
      });
    });

    // Intercept me api call
    await page.route('**/api/auth/me', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          userId: 'user-002',
          email: 'newuser@example.com',
          name: 'New QA Tester',
          role: 'DEVELOPER',
        }),
      });
    });

    await page.goto('/signup');
    await page.fill('input[type="text"]', 'New QA Tester');
    await page.fill('input[type="email"]', 'newuser@example.com');
    await page.fill('input[type="password"]', 'securepassword123');
    await page.click('button[type="submit"]');

    // Should redirect to dashboard (/)
    await expect(page).toHaveURL('/');
  });

  test('expired session token redirects to login', async ({ page }) => {
    // Intercept me api call to return 401 Unauthorized (expired token)
    await page.route('**/api/auth/me', async (route) => {
      await route.fulfill({
        status: 401,
        contentType: 'application/json',
        body: JSON.stringify({
          error: 'UNAUTHORIZED',
          message: 'Token has expired',
        }),
      });
    });

    await page.goto('/');

    // Should be redirected back to /login
    await expect(page).toHaveURL(/\/login/);
  });
});
