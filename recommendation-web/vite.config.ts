import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
const apiProxy = {
  '/api': {
    target: 'http://127.0.0.1:8080',
    changeOrigin: true,
  },
} as const

export default defineConfig({
  plugins: [react()],
  server: {
    proxy: { ...apiProxy },
  },
  /** Same proxy for `npm run preview` so /api hits Spring (dev uses `npm run dev`). */
  preview: {
    proxy: { ...apiProxy },
  },
})
