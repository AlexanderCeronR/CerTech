import { defineConfig, loadEnv } from 'vite';
import react from '@vitejs/plugin-react';
import fs from 'node:fs';
import { validateBuildApi } from './security/api-policy.mjs';

export default defineConfig(({ command, mode }) => {
  const env = loadEnv(mode, process.cwd(), 'VITE_');
  const config = JSON.parse(fs.readFileSync(new URL('./vercel.json', import.meta.url)));
  const api = command === 'build'
    ? validateBuildApi(env.VITE_API_URL || '', config, process.env.VERCEL === '1')
    : env.VITE_API_URL || '';
  return {
    plugins: [react()],
    // Expose only the reviewed API URL; arbitrary VITE_* values stay out of the bundle.
    envPrefix: [],
    define: { 'import.meta.env.VITE_API_URL': JSON.stringify(api) },
    build: { outDir: 'dist', sourcemap: false },
    server: {
      port: 5173,
      proxy: { '/api': { target: process.env.VITE_PROXY_TARGET || 'http://localhost:8080' } },
    },
  };
});
