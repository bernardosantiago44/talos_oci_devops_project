/** @type {import('@hey-api/openapi-ts').UserConfig} */
module.exports = {
  input: 'src/api-spec/openapi.json',
  output: 'src/api/generated',
  plugins: [
    {
      name: '@hey-api/client-fetch',
      baseUrl: false,
    },
    '@hey-api/sdk',
  ],
};
