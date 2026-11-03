import { defineConfig, loadEnv } from "vite";
import vue from "@vitejs/plugin-vue";

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, ".", "");
  return {
    plugins: [vue()],
    server: {
      ...(env.FRONTEND_PORT
        ? { port: Number(env.FRONTEND_PORT), strictPort: true }
        : {}),
      proxy: { "/api": { target: env.BACKEND_URL || "http://127.0.0.1:8080", changeOrigin: true } },
    },
  };
});
