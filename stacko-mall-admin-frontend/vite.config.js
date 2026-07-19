import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
export default defineConfig({
    plugins: [react()],
    server: {
        port: 5174,
        proxy: {
            '/api/auth': {
                target: process.env.VITE_USER_CENTER_TARGET || 'http://localhost:8080',
                changeOrigin: true,
                secure: false
            },
            '/api': {
                target: process.env.VITE_PROXY_TARGET || 'http://localhost:8081',
                changeOrigin: true,
                secure: false
            }
        }
    }
});
