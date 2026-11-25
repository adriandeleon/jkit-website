# jkit website

The website for [jkit](https://github.com/adriandeleon/jkit), served by GitHub Pages at
<https://jkit-project.dev>. Built with [Astro](https://astro.build) and
[Starlight](https://starlight.astro.build).

```bash
npm install
npm run dev       # http://localhost:4321/
npm run build     # output in dist/; fails on broken internal links
```

Pushing to `main` builds and deploys the site (`.github/workflows/pages.yml`). The site describes
the *released* jkit, so merge changes for a new jkit feature when that release ships.

| What | Where |
| --- | --- |
| Home page | `src/content/docs/index.mdx` |
| Docs pages | `src/content/docs/{start,why,guides,reference,help}/` |
| Sidebar, plugins, redirects | `astro.config.mjs` |
| What's new (changelog) | generated from jkit's `CHANGELOG.md` by `scripts/GenWhatsNew.java`: `src/content/docs/whats-new.md`, `whats-new/x.y.md`, `src/lib/releases.ts` |
| News posts | `src/content/docs/news/*.md` (`draft: true` keeps a post out of the build) |
| Brand colours | `src/styles/theme.css` |

Plugins: [starlight-blog](https://github.com/HiDeoo/starlight-blog) for News (with an RSS feed at
`/news/rss.xml`), [starlight-llms-txt](https://github.com/delucis/starlight-llms-txt) for
`/llms.txt`, `/llms-full.txt` and `/llms-small.txt`, and
[starlight-links-validator](https://github.com/HiDeoo/starlight-links-validator) to check links.

## At each jkit release

1. Add a News post (or take `draft: true` off the one prepared for it), set its date, and give it
   `version: "x.y.z"` so What's new links to it as the release's announcement.
2. Regenerate What's new from the release's `CHANGELOG.md` (JDK 25; the jkit checkout is found as
   `../jkit`, or pass its path or set `JKIT_REPO`). Only released sections are used; `[Unreleased]`
   is skipped.

   ```bash
   java scripts/GenWhatsNew.java
   ```
3. Update pages for new or changed commands (`reference/commands/`) and the "not yet" notes
   (search the docs for "not yet" and "preview").
4. Once the site is deployed, post the News post in
   [Discussions › Announcements](https://github.com/adriandeleon/jkit/discussions/categories/announcements):
   its title, its body with site links made absolute (`https://jkit-project.dev/...`), and a last
   line linking the full announcement, the release's What's new page and the release page.
