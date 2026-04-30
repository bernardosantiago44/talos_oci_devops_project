const path = require('path');

module.exports = {
    devServer: (devServerConfig) => {
        devServerConfig.host = '127.0.0.1';
        return devServerConfig;
    },
    webpack: {
        alias: {
            '@': path.resolve(__dirname, 'src'),
        },
        configure: (webpackConfig) => {
            const extensions = webpackConfig.resolve?.extensions ?? [];
            const preferred = ['.tsx', '.ts'];
            const remaining = extensions.filter((extension) => !preferred.includes(extension));

            webpackConfig.resolve = {
                ...webpackConfig.resolve,
                extensions: [...preferred, ...remaining],
            };

            return webpackConfig;
        },
    },
    style: {
        postcss: {
            mode: "extends",
            loaderOptions: {
                postcssOptions: {
                    ident: 'postcss',
                    plugins: [
                        require('tailwindcss'),
                        require('autoprefixer'),
                    ],
                },
            },
        },
    },
};
