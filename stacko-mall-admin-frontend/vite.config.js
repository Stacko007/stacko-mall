import { defineConfig, loadEnv } from 'vite';
import react from '@vitejs/plugin-react';
export default defineConfig(function (_a) {
    var mode = _a.mode;
    var env = loadEnv(mode, '.', '');
    var gatewayTarget = env.VITE_GATEWAY_TARGET || 'http://localhost:8088';
    return {
        plugins: [react()],
        server: {
            port: 5174,
            proxy: {
                '/user': {
                    target: gatewayTarget,
                    changeOrigin: true,
                    secure: false
                },
                '/mall': {
                    target: gatewayTarget,
                    changeOrigin: true,
                    secure: false
                }
            }
        }
    };
});
