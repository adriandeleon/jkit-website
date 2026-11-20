export const repo = "https://github.com/adriandeleon/jkit";
export const releases = `${repo}/releases/latest`;
export const vitrina = "https://vitrina-project.dev";

const base = import.meta.env.BASE_URL;
/** Site-relative link that respects the configured base path. */
export const url = (p = "") => `${base}/${p}`.replace(/\/{2,}/g, "/");
