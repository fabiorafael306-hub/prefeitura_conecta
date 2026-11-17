/// <reference types="vitest" />
import { defineConfig, loadEnv } from 'vite';
import react from '@vitejs/plugin-react';

// Em desenvolvimento, /api é encaminhado ao backend (sem CORS no navegador).
// O alvo pode ser alterado com VITE_BACKEND_URL no arquivo .env.local.
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '');
  return {
    plugins: [react()],
    server: {
      port: 5173,
      strictPort: true,
      proxy: { '/api': { target: env.VITE_BACKEND_URL || 'http://localhost:8080', changeOrigin: true } },
    },
    test: { environment: 'jsdom' },
  };
});
