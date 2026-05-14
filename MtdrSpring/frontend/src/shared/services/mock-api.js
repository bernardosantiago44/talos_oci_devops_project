const DEFAULT_DELAY_MS = 250;
export async function mockApi(data, delayMs = DEFAULT_DELAY_MS) {
    await new Promise((resolve) => setTimeout(resolve, delayMs));
    return {
        success: true,
        data: structuredClone(data)
    };
}
