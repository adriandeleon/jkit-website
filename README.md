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
| What's new (changelog) | `src/content/docs/whats-new.md` |
| News posts | `src/content/docs/news/*.md` (`draft: true` keeps a post out of the build) |
| Brand colours | `src/styles/theme.css` |

Plugins: [starlight-blog](https://github.com/HiDeoo/starlight-blog) for News (with an RSS feed at
`/news/rss.xml`), [starlight-llms-txt](https://github.com/delucis/starlight-llms-txt) for
`/llms.txt`, `/llms-full.txt` and `/llms-small.txt`, and
[starlight-links-validator](https://github.com/HiDeoo/starlight-links-validator) to check links.

## At each jkit release

1. Add a section to `src/content/docs/whats-new.md`.
2. Add a News post (or take `draft: true` off the one prepared for it) and set its date.
3. Update pages for new or changed commands (`reference/commands/`) and the "not yet" notes
   (search the docs for "not yet" and "preview").
