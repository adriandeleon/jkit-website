// @ts-check
import { defineConfig } from "astro/config";
import starlight from "@astrojs/starlight";
import starlightBlog from "starlight-blog";
import starlightLlmsTxt from "starlight-llms-txt";
import starlightLinksValidator from "starlight-links-validator";

// Served by GitHub Pages at the root of https://jkit-project.dev (public/CNAME).
// Starlight adds the sitemap itself, because `site` is set.
export default defineConfig({
  site: "https://jkit-project.dev",
  // The old single-page URLs keep working.
  redirects: {
    "/install": "/start/install/",
    "/docs": "/start/introduction/",
  },
  integrations: [
    starlight({
      title: "jkit",
      description:
        "jkit installs and switches JDKs from 20+ vendors, Maven, Gradle and sbt: per shell, per project or globally. One native binary for Linux, macOS and Windows; no JDK needed to run it.",
      logo: {
        light: "./src/assets/wordmark-light.svg",
        dark: "./src/assets/wordmark-dark.svg",
        replacesTitle: true,
        alt: "jkit",
      },
      favicon: "/favicon.svg",
      social: [
        { icon: "comment-alt", label: "Discussions", href: "https://github.com/adriandeleon/jkit/discussions" },
        { icon: "github", label: "GitHub", href: "https://github.com/adriandeleon/jkit" },
      ],
      editLink: { baseUrl: "https://github.com/adriandeleon/jkit-website/edit/main/" },
      lastUpdated: true,
      customCss: ["./src/styles/theme.css"],
      components: { Footer: "./src/components/Footer.astro" },
      head: [
        { tag: "meta", attrs: { property: "og:image", content: "https://jkit-project.dev/og.png" } },
        { tag: "meta", attrs: { property: "og:image:width", content: "1280" } },
        { tag: "meta", attrs: { property: "og:image:height", content: "640" } },
        { tag: "meta", attrs: { name: "twitter:card", content: "summary_large_image" } },
      ],
      plugins: [
        starlightLinksValidator({ errorOnLocalLinks: false, exclude: ["/news/", "/news/**", "/llms*.txt"] }),
        starlightBlog({
          title: "News",
          prefix: "news",
          navigation: "header-end",
          authors: {
            adrian: { name: "Adrian De Leon", url: "https://github.com/adriandeleon" },
          },
        }),
        starlightLlmsTxt({
          projectName: "jkit",
          description:
            "jkit is a single native command-line tool that installs and switches JDKs from 20+ vendors, GraalVM, Maven, Gradle and sbt, per shell, per project (jkit.toml) or globally, on Linux, macOS and Windows.",
          details:
            "Agents and scripts should run builds through `jkit exec -- <command>`: each agent command starts a new shell, so `jkit use` without --global does not persist. Add `--json` for one machine-readable document on stdout. Exit codes: 0 ok, 1 failure, 2 bad arguments, 3 version not installed. No jkit command ever prompts.",
          promote: ["start/**", "guides/ai-agents*", "reference/**"],
          demote: ["whats-new*"],
          exclude: ["news", "news/**"],
        }),
      ],
      sidebar: [
        {
          label: "Start here",
          items: ["start/introduction", "start/install", "start/quickstart", "start/java-basics"],
        },
        {
          label: "Why jkit",
          items: ["why/overview", "why/vs-manual", "why/vs-sdkman", "why/vs-mise-asdf", "why/vs-os-packages"],
        },
        { label: "Guides", items: [{ autogenerate: { directory: "guides" } }] },
        {
          label: "Reference",
          items: [
            "reference/versions-and-vendors",
            "reference/project-files",
            "reference/configuration",
            "reference/shell-hook",
            "reference/files-and-folders",
            "reference/json",
            "reference/exit-codes",
            "reference/security",
            { label: "Commands", collapsed: true, items: [{ autogenerate: { directory: "reference/commands" } }] },
          ],
        },
        { label: "Help", items: [{ autogenerate: { directory: "help" } }] },
        "whats-new",
        "llms",
      ],
    }),
  ],
  vite: {
    server: { watch: { ignored: ["**/dist/**", "**/.claude/**"] } },
  },
});
