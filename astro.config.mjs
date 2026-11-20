// @ts-check
import { defineConfig } from "astro/config";
import sitemap from "@astrojs/sitemap";

// Served by GitHub Pages at https://adriandeleon.github.io/jkit-website/.
// With a custom domain later: set `site` to it, drop `base`, add public/CNAME.
export default defineConfig({
  site: "https://adriandeleon.github.io",
  base: "/jkit-website",
  integrations: [sitemap()],
  vite: {
    server: { watch: { ignored: ["**/dist/**", "**/.claude/**"] } },
  },
});
