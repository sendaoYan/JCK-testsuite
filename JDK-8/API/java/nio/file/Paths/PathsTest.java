/*
 * @test
 * @summary java.nio.file.Paths conformance: white-box structure (reflection) + black-box behavior
 *          (get(String,...) and get(URI), multi-arg joining and empty path).
 * @run main PathsTest
 */

import java.lang.reflect.Modifier;
import java.net.URI;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PathsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface Op { void run() throws Exception; }

    /** Negative-path helper: asserts the EXACT exception class (no subclasses). */
    static void checkThrows(Class<? extends Throwable> ex, Op r, String msg) {
        try { r.run(); }
        catch (Throwable t) {
            if (t.getClass() != ex)
                throw new AssertionError(msg + ": expected " + ex.getName() + " got " + t);
            passed++;
            return;
        }
        throw new AssertionError(msg + ": expected " + ex.getName() + " but nothing thrown");
    }

    static void hasMethod(Class<?> c, String name, Class<?>... params) {
        try { c.getMethod(name, params); }
        catch (NoSuchMethodException e) { throw new AssertionError("missing method " + name); }
        passed++;
    }

    static final String SEP = java.io.File.separator;

    static Path abs(String... names) {
        Path p = java.nio.file.FileSystems.getDefault().getRootDirectories().iterator().next();
        for (String n : names) p = p.resolve(n);
        return p;
    }

    // ---- white-box: structure via reflection ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.file.Paths");
        check(!c.isInterface() && Modifier.isFinal(c.getModifiers()), "final class");
        check(c.getSuperclass() == Object.class, "superclass Object");
        hasMethod(c, "get", String.class, String[].class);
        hasMethod(c, "get", URI.class);
    }

    // ---- black-box: behavior ----
    static void testBlackBox() {
        Path p = Paths.get("a", "b", "c");
        check(p.getNameCount() == 3, "getNameCount 3");
        check("c".equals(p.getFileName().toString()), "getFileName");
        check("a".equals(p.getName(0).toString()), "getName(0)");

        check(Paths.get("a", "b", "c").equals(Paths.get("a/b/c")), "multi-arg equals joined");
        check(Paths.get("a", "b").equals(Paths.get("a", "b")), "repeat get equals");
        check(Paths.get("a", "b").toString().equals("a" + SEP + "b"), "joined toString");
        check("".equals(Paths.get("").toString()), "empty path toString empty");

        Path uriAbs = Paths.get(abs("tmp", "x").toUri());
        check(uriAbs.isAbsolute(), "get(URI) absolute");
        check(uriAbs.equals(abs("tmp", "x")), "get(URI) round-trip");

        boolean npe = false;
        try { Paths.get((String) null); } catch (NullPointerException e) { npe = true; }
        check(npe, "get(null) NPE");

        boolean npe2 = false;
        try { Paths.get((URI) null); } catch (NullPointerException e) { npe2 = true; }
        check(npe2, "get((URI)null) NPE");
    }

    // ===== deepened contract coverage (batch A9) =====

    // multi-segment joining + separator handling + embedded separators
    static void testJoinMatrix() {
        check(Paths.get("a", "b", "c", "d").getNameCount() == 4, "four segments");
        check(Paths.get("/a", "b").equals(Paths.get("/a/b")), "root segment joined");
        check(Paths.get("a", "").equals(Paths.get("a")), "empty trailing segment tolerated");
        check(Paths.get("a/b", "c").equals(Paths.get("a/b/c")), "embedded separator in segment");
        check(Paths.get("a", "b").toString().equals("a" + SEP + "b"), "joined toString a/b");
        check(Paths.get("a", "b", "c").isAbsolute() == false, "relative join stays relative");
        check(abs("tmp", "x").isAbsolute(), "absolute first segment absolute");
        check(Paths.get("a/b/c").equals(Paths.get("a", "b", "c")), "single equals multi");
        check(abs().getNameCount() == 0, "root only");
        // NUL byte is rejected with a precise exception (pre-verified fact)
        try {
            Paths.get("a\0b");
            check(false, "NUL in path must throw InvalidPathException");
        } catch (InvalidPathException e) {
            // -1 means "unknown position"; the Windows provider reports the
            // offset of the offending NUL instead.
            int idx = e.getIndex();
            check(idx == -1 || (idx >= 0 && idx < "a\0b".length()),
                  "InvalidPathException index is -1 or a valid offset: " + idx);
            check(e.getInput() != null && e.getInput().contains("a"), "InvalidPathException input");
        }
        checkThrows(NullPointerException.class, () -> Paths.get("a", (String[]) null),
              "get(first, null more) NPE");
        checkThrows(NullPointerException.class, () -> Paths.get("a", new String[] {null}),
              "get(first, null element) NPE");
        // resulting Path belongs to the default FileSystem
        check(Paths.get("x").getFileSystem() == java.nio.file.FileSystems.getDefault(),
              "result bound to default FileSystem");
    }

    // URI conversion matrix (platform-neutral via Path.toUri round-trip)
    static void testUriMatrix() {
        Path absPath = abs("tmp", "x");
        URI absUri = absPath.toUri();
        check(Paths.get(absUri).isAbsolute(), "get(URI) absolute");
        check(Paths.get(absUri).equals(absPath), "get(URI) round-trip");
        Path spaced = abs("a b");
        check(Paths.get(spaced.toUri()).equals(spaced), "file URI decodes %20 round-trip");
        check(Paths.get(spaced.toUri()).toUri().equals(spaced.toUri()),
              "escape round-trip");
        check(Paths.get(absUri).getFileSystem()
              == java.nio.file.FileSystems.getDefault(), "get(URI) default provider");
        // negatives with exact types (probe-confirmed on both JDKs)
        checkThrows(IllegalArgumentException.class, () -> Paths.get(URI.create("file:a/b")),
              "file: relative URI -> IllegalArgumentException (not hierarchical)");
        if (java.io.File.separatorChar == '\\') {
            // Windows 把 file://host/a 解析为合法的 UNC 路径 \\host\a（探针实测）。
            Path unc = Paths.get(URI.create("file://host/a"));
            check(unc.isAbsolute() && unc.toString().startsWith("\\\\host"),
                  "file URI with authority is a UNC path on Windows");
        } else {
            checkThrows(IllegalArgumentException.class, () -> Paths.get(URI.create("file://host/a")),
                  "file URI with authority -> IllegalArgumentException");
        }
        checkThrows(java.nio.file.FileSystemNotFoundException.class,
              () -> Paths.get(URI.create("jar:file:///tmp/nope.zip!/e")),
              "jar: URI provider installed but no fs open -> FileSystemNotFoundException");
        checkThrows(java.nio.file.FileSystemNotFoundException.class,
              () -> Paths.get(URI.create("bogus://x/y")),
              "unknown scheme bogus: -> FileSystemNotFoundException (provider not installed)");
        checkThrows(java.nio.file.FileSystemNotFoundException.class,
              () -> Paths.get(URI.create("http://h/p")),
              "http: scheme has no provider -> FileSystemNotFoundException");
    }

    // JDK-8 has no Path.of (delegating factory added in JDK 11); Paths.get stays the entry point
    static void testJdk8NoPathOf() throws Exception {
        boolean absent = false;
        try { Class.forName("java.nio.file.Path").getMethod("of", String.class, String[].class); }
        catch (NoSuchMethodException e) { absent = true; }
        check(absent, "JDK-8 has no Path.of(String,String...)");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testJoinMatrix();
        testUriMatrix();
        testJdk8NoPathOf();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
