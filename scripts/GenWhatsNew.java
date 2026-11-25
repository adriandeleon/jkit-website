/// Generates the What's new pages from jkit's CHANGELOG.md.
/// A JDK 25 compact source file (JEP 512): run `java scripts/GenWhatsNew.java` from the website
/// repo root. The jkit checkout is found as a sibling directory (../jkit); override it with an
/// argument or the JKIT_REPO environment variable.
///
/// It writes committed files, so the site build needs no access to the jkit repo:
///   src/content/docs/whats-new.md          the newest release series, at the stable URL
///   src/content/docs/whats-new/x.y.md      one page per series, at /whats-new/x.y/
///   src/lib/releases.ts                    the series list, release dates and announcements
///
/// The changelog follows Keep a Changelog: "## [x.y.z] - yyyy-mm-dd" per release, with
/// "### Added", "### Changed", "### Fixed" and "### Security" under it. [Unreleased] is left out.
/// A news post announces a release when its frontmatter has `version: "x.y.z"`.
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

record Release(String version, String date, String body) {}
record Series(String version, List<Release> releases) {}

static final String SITE = "https://jkit-project.dev";
static final String REPO = "https://github.com/adriandeleon/jkit";
static final Pattern RELEASE = Pattern.compile("(?m)^## \\[([^\\]]+)\\](?:\\s*-\\s*(\\d{4}-\\d{2}-\\d{2}))?\\s*$");
static final Pattern LINK_DEF = Pattern.compile("(?m)^\\[[^\\]]+\\]:\\s*\\S+\\s*$");
static final Pattern SITE_LINK = Pattern.compile("\\]\\(" + Pattern.quote(SITE) + "(/[^)]*)\\)");
// A link to a file in the jkit repository (`docs/json.md`) is relative to the repository root,
// which on the website resolves under /whats-new/ and 404s. Point it at the file on GitHub as of
// the release's tag, so the link keeps describing what that release shipped.
static final Pattern REPO_LINK = Pattern.compile("\\]\\(((?:[A-Za-z0-9_.-]+/)*[A-Za-z0-9_-][A-Za-z0-9_.-]*\\.[A-Za-z]+)\\)");
static final Pattern NEWS_VERSION = Pattern.compile("(?m)^version:\\s*\"?([^\"\\s]+)\"?\\s*$");
static final DateTimeFormatter LONG_DATE = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH);

List<Release> releases(String changelog) {
    List<Release> out = new ArrayList<>();
    Matcher m = RELEASE.matcher(changelog);
    List<int[]> starts = new ArrayList<>();
    List<String[]> heads = new ArrayList<>();
    while (m.find()) {
        starts.add(new int[] {m.start(), m.end()});
        heads.add(new String[] {m.group(1), m.group(2)});
    }
    for (int i = 0; i < starts.size(); i++) {
        String version = heads.get(i)[0];
        if (version.equalsIgnoreCase("Unreleased")) continue;
        if (heads.get(i)[1] == null) throw new IllegalStateException("release " + version + " has no date");
        int end = i + 1 < starts.size() ? starts.get(i + 1)[0] : changelog.length();
        String body = LINK_DEF.matcher(changelog.substring(starts.get(i)[1], end)).replaceAll("").strip();
        out.add(new Release(version, heads.get(i)[1], body));
    }
    return out;
}

String seriesOf(String version) {
    String[] parts = version.split("\\.");
    return parts[0] + "." + parts[1];
}

String links(String md, String version) {
    md = SITE_LINK.matcher(md).replaceAll(m -> Matcher.quoteReplacement("](" + m.group(1) + ")"));
    return REPO_LINK.matcher(md).replaceAll(m ->
        Matcher.quoteReplacement("](" + REPO + "/blob/v" + version + "/" + m.group(1) + ")"));
}

String longDate(String iso) {
    return LocalDate.parse(iso).format(LONG_DATE);
}

String yaml(String s) {
    return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
}

String ts(String s) {
    return yaml(s);
}

String seriesHref(Series s, boolean newest) {
    return newest ? "/whats-new/" : "/whats-new/" + s.version() + "/";
}

String page(Series s, List<Series> all, Map<String, String> posts, boolean stable) {
    Release newest = s.releases().getFirst();
    Release oldest = s.releases().getLast();
    String range = s.releases().size() == 1
        ? "jkit " + newest.version() + " (" + longDate(newest.date()) + ")"
        : "jkit " + oldest.version() + " to " + newest.version();
    boolean newestSeries = all.getFirst() == s;
    StringBuilder md = new StringBuilder("---\n");
    md.append("title: ").append(yaml(stable ? "What's new" : "What's new in " + s.version() + ".x")).append('\n');
    md.append("description: ").append(yaml("Release notes for " + range
        + ", from the changelog: everything added, changed and fixed.")).append('\n');
    if (!stable) md.append("slug: whats-new/").append(s.version()).append('\n');
    if (!stable && newestSeries) {
        // The same notes as /whats-new/ until the next series takes over that URL.
        md.append("pagefind: false\n");
        md.append("head:\n  - tag: link\n    attrs:\n      rel: canonical\n      href: ")
            .append(SITE).append("/whats-new/\n");
    }
    md.append("tableOfContents:\n  maxHeadingLevel: 2\n");
    md.append("---\n\n");
    md.append("<!-- AUTO-GENERATED by scripts/GenWhatsNew.java from jkit's CHANGELOG.md. Do not edit by hand. -->\n\n");
    md.append("Everything in the jkit ").append(s.version()).append(".x releases, from the [changelog](")
        .append(REPO).append("/blob/main/CHANGELOG.md). ");
    md.append("Each release's announcement, with the highlights, is in [News](/news/). ");
    md.append("To update, run `jkit self-update` (see [Keeping up to date](/guides/keeping-up-to-date/)).\n\n");
    StringBuilder others = new StringBuilder();
    for (Series o : all) {
        if (o == s) continue;
        if (!others.isEmpty()) others.append(" · ");
        others.append("[").append(o.version()).append(".x](").append(seriesHref(o, o == all.getFirst())).append(")");
    }
    if (!others.isEmpty()) md.append("Other release series: ").append(others).append(".\n\n");
    for (Release r : s.releases()) {
        md.append("## ").append(r.version()).append("\n\n");
        md.append("*").append(longDate(r.date())).append("*");
        String post = posts.get(r.version());
        if (post != null) md.append(" · [Announcement](").append(post).append(")");
        md.append(" · [Download](").append(REPO).append("/releases/tag/v").append(r.version()).append(")\n\n");
        if (!r.body().isEmpty()) md.append(links(r.body(), r.version())).append("\n\n");
    }
    return md.toString().stripTrailing() + "\n";
}

void main(String[] args) throws IOException {
    Path web = Path.of("").toAbsolutePath();
    if (web.getFileName().toString().equals("scripts")) web = web.getParent();
    String repoArg = args.length > 0 ? args[0] : System.getenv("JKIT_REPO");
    Path jkit = repoArg != null ? Path.of(repoArg) : web.resolveSibling("jkit");
    Path changelogPath = jkit.resolve("CHANGELOG.md");
    if (!Files.isRegularFile(changelogPath)) {
        throw new IllegalStateException("no CHANGELOG.md in " + jkit + " (pass the jkit checkout or set JKIT_REPO)");
    }
    List<Release> releases = releases(Files.readString(changelogPath, StandardCharsets.UTF_8));
    if (releases.isEmpty()) throw new IllegalStateException("CHANGELOG.md has no released versions");

    Map<String, List<Release>> bySeries = new LinkedHashMap<>();
    for (Release r : releases) bySeries.computeIfAbsent(seriesOf(r.version()), k -> new ArrayList<>()).add(r);
    List<Series> series = new ArrayList<>();
    bySeries.forEach((v, rs) -> series.add(new Series(v, rs)));

    // News posts that announce a release, by version.
    Map<String, String> posts = new TreeMap<>();
    Path news = web.resolve("src/content/docs/news");
    try (var files = Files.list(news)) {
        for (Path f : files.filter(p -> p.toString().matches(".*\\.mdx?$")).toList()) {
            String text = Files.readString(f, StandardCharsets.UTF_8);
            int end = text.indexOf("\n---", 3);
            if (!text.startsWith("---") || end < 0) continue;
            Matcher m = NEWS_VERSION.matcher(text.substring(0, end));
            if (m.find()) {
                String name = f.getFileName().toString().replaceFirst("\\.mdx?$", "");
                posts.put(m.group(1), "/news/" + name + "/");
            }
        }
    }

    Path docs = web.resolve("src/content/docs");
    Files.writeString(docs.resolve("whats-new.md"), page(series.getFirst(), series, posts, true), StandardCharsets.UTF_8);
    Path dir = docs.resolve("whats-new");
    Files.createDirectories(dir);
    try (var old = Files.list(dir)) {
        for (Path p : old.filter(p -> p.getFileName().toString().matches("[0-9]+\\.[0-9]+\\.md")).toList()) {
            Files.delete(p);
        }
    }
    for (Series s : series) {
        Files.writeString(dir.resolve(s.version() + ".md"), page(s, series, posts, false), StandardCharsets.UTF_8);
    }

    StringBuilder lib = new StringBuilder();
    lib.append("// AUTO-GENERATED by scripts/GenWhatsNew.java from jkit's CHANGELOG.md. Do not edit by hand.\n");
    lib.append("// The newest series is at /whats-new/ (and /whats-new/x.y/); older ones at /whats-new/x.y/.\n\n");
    lib.append("export const releaseSeries = [\n");
    for (Series s : series) {
        lib.append("  { version: ").append(ts(s.version())).append(", label: ").append(ts(s.version() + ".x"))
            .append(", href: ").append(ts(seriesHref(s, s == series.getFirst()))).append(" },\n");
    }
    lib.append("] as const;\n\n");
    lib.append("export const releases: Record<string, { date: string; post?: string }> = {\n");
    for (Release r : releases) {
        lib.append("  ").append(ts(r.version())).append(": { date: ").append(ts(r.date()));
        String post = posts.get(r.version());
        if (post != null) lib.append(", post: ").append(ts(post));
        lib.append(" },\n");
    }
    lib.append("};\n");
    Path libDir = web.resolve("src/lib");
    Files.createDirectories(libDir);
    Files.writeString(libDir.resolve("releases.ts"), lib.toString(), StandardCharsets.UTF_8);

    IO.println("Wrote What's new: " + releases.size() + " releases in " + series.size() + " series, "
        + posts.size() + " announced");
}
