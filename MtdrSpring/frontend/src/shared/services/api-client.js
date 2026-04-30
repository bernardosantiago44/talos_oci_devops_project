/**
 * Generic HTTP client for the Spring Boot backend.
 * The React dev-server proxy (package.json → "proxy": "http://localhost:8080")
 * forwards all unmatched requests to the backend, so we use relative URLs.
 */
const API_BASE_PATH = '/api';
function withApiBasePath(url) {
    if (/^https?:\/\//i.test(url) || url.startsWith(API_BASE_PATH))
        return url;
    return `${API_BASE_PATH}${url.startsWith('/') ? url : `/${url}`}`;
}
async function request(url, options = {}) {
    try {
        const res = await fetch(withApiBasePath(url), {
            headers: { 'Content-Type': 'application/json', ...options.headers },
            ...options,
        });
        if (!res.ok) {
            const errorText = await res.text().catch(() => res.statusText);
            return {
                success: false,
                data: null,
                message: `HTTP ${res.status}: ${errorText}`,
            };
        }
        // Handle 204 No Content
        if (res.status === 204) {
            return { success: true, data: null };
        }
        const data = await res.json();
        return { success: true, data };
    }
    catch (err) {
        const message = err instanceof Error ? err.message : 'Network error';
        return { success: false, data: null, message };
    }
}
export const apiClient = {
    get(url) {
        return request(url);
    },
    post(url, body) {
        return request(url, { method: 'POST', body: JSON.stringify(body) });
    },
    put(url, body) {
        return request(url, { method: 'PUT', body: JSON.stringify(body) });
    },
    delete(url) {
        return request(url, { method: 'DELETE' });
    },
};
