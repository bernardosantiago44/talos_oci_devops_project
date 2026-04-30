export async function readData<T>(request: Promise<{ data: T }>): Promise<T> {
  const response = await request;
  return response.data;
}
