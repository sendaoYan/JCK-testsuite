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
        check("a/b".equals(Paths.get("a", "b").toString()), "joined toString");
        check("".equals(Paths.get("").toString()), "empty path toString empty");

        Path abs = Paths.get(URI.create("file:///tmp/x"));
        check(abs.isAbsolute(), "get(URI) absolute");
        check("/tmp/x".equals(abs.toString()), "get(URI) path");

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
        check(Paths.get("a", "b").toString().equals("a/b"), "joined toString a/b");
        check(Paths.get("a", "b", "c").isAbsolute() == false, "relative join stays relative");
        check(Paths.get("/tmp", "x").isAbsolute(), "absolute first segment absolute");
        check(Paths.get("a/b/c").equals(Paths.get("a", "b", "c")), "single equals multi");
        check(Paths.get("/").getNameCount() == 0, "root only");
        // NUL byte is rejected with a precise exception (pre-verified fact)
        try {
            Paths.get("a\0b");
            check(false, "NUL in path must throw InvalidPathException");
        } catch (InvalidPathException e) {
            check(e.getIndex() == -1, "InvalidPathException index -1 for NUL");
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

    // URI conversion matrix (probe-confirmed exact types)
    static void testUriMatrix() {
        Path abs = Paths.get(URI.create("file:///tmp/x"));
        check(abs.isAbsolute(), "file:///tmp/x absolute");
        check("/tmp/x".equals(abs.toString()), "file:///tmp/x decodes to /tmp/x");
        check(Paths.get(URI.create("file:///a%20b")).equals(Paths.get("/a b")),
              "file URI decodes %20");
        check(Paths.get(URI.create("file:///a%20b")).toUri().equals(URI.create("file:///a%20b")),
              "escape round-trip a b");
        check(Paths.get(URI.create("file:///")).equals(Paths.get("/")), "file:/// -> root");
        check(Paths.get(URI.create("file:///a/b")).equals(
                java.nio.file.FileSystems.getDefault().getPath("/a/b")),
              "get(URI) equals default fs getPath");
        check(Paths.get(URI.create("file:///tmp/x")).getFileSystem()
              == java.nio.file.FileSystems.getDefault(), "get(URI) default provider");
        // negatives with exact types (probe-confirmed on both JDKs)
        checkThrows(IllegalArgumentException.class, () -> Paths.get(URI.create("file:a/b")),
              "file: relative URI -> IllegalArgumentException (not hierarchical)");
        checkThrows(IllegalArgumentException.class, () -> Paths.get(URI.create("file://host/a")),
              "file URI with authority -> IllegalArgumentException");
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
