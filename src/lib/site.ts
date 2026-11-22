/** The latest jkit release; the only line to change when a new one comes out. */
export const version = "0.2.2";

export const repo = "https://github.com/adriandeleon/jkit";
export const releases = `${repo}/releases/latest`;
/** A doc in the jkit repo as of the latest release, so it never describes unreleased commands. */
export const repoDoc = (name: string) => `${repo}/blob/v${version}/docs/${name}`;
export const vitrina = "https://vitrina-project.dev";

const base = import.meta.env.BASE_URL;
/** Site-relative link that respects the configured base path. */
export const url = (p = "") => `${base}/${p}`.replace(/\/{2,}/g, "/");
