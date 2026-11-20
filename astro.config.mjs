// @ts-check
import { defineConfig } from "astro/config";
import sitemap from "@astrojs/sitemap";

// Served by GitHub Pages at the root of https://jkit-project.dev (public/CNAME).
export default defineConfig({
  site: "https://jkit-project.dev",
  integrations: [sitemap()],
  vite: {
    server: { watch: { ignored: ["**/dist/**", "**/.claude/**"] } },
  },
});
