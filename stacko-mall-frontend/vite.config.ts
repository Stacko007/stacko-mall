import { defineConfig, loadEnv } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, '.', '');
  const gatewayTarget = env.VITE_GATEWAY_TARGET || 'http://localhost:8088';

  return {
    plugins: [react()],
    server: {
      port: 5173,
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
