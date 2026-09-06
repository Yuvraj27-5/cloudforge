import { defineConfig } from "vite"
import react from "@vitejs/plugin-react"

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      // Dev-only: keeps the browser on one origin so CORS never bites locally.
      "/api": {
        target: "http://localhost:8080",
        changeOrigin: true,
      },
    },
  },
})
