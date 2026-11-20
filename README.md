# jkit website

The website for [jkit](https://github.com/adriandeleon/jkit), served by GitHub Pages at
<https://adriandeleon.github.io/jkit-website/>. Built with [Astro](https://astro.build).

```bash
npm install
npm run dev       # http://localhost:4321/jkit-website/
npm run build     # output in dist/
```

Pushing to `main` builds and deploys the site (`.github/workflows/pages.yml`).

| Page | File |
| --- | --- |
| Home | `src/pages/index.astro` |
| Install | `src/pages/install.astro` |
| Docs | `src/pages/docs.astro` |
| Shared layout and styles | `src/layouts/Layout.astro` |
