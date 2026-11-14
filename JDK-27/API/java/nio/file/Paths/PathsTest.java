/*
 * @test
 * @summary java.nio.file.Paths conformance: white-box structure (reflection) + black-box behavior
 *          (get(String,...) and get(URI), equivalence with Path.of).
 * @run main PathsTest
 *
 * 黑盒设计案:
 * 1. 契约: 构造 Path 的便捷工厂，get(String,String...) 拼接分量，get(URI) 从 URI 构造。
 * 2. 场景: Paths.get("a","b","c") -> 分量断言；Paths.get(URI) -> 绝对路径。
 * 3. 组合: Paths.get 与 Path.of 等价断言；多参拼接与单参带分隔符一致。
 * 4. 不变量: get 返回与 Path.of 相同系统 Provider 的 Path。
 * 5. 边界: 空字符串 Paths.get("") 的 nameCount==0。
 */

import java.lang.reflect.Modifier;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PathsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void hasMethod(Class<?> c, String name, Class<?>... params) {
        try { c.getMethod(name, params); }
        catch (NoSuchMethodException e) { throw new AssertionError("missing method " + name); }
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
        check(Paths.get("a", "b").equals(Path.of("a", "b")), "Paths.get equals Path.of");
        check("".equals(Paths.get("").toString()), "empty path toString empty");

        // A file: URI round-trips this platform's own absolute path form; the
        // POSIX "/tmp/x" spelling is a corner of that only where "/" is root.
        Path tmpDir = Paths.get(System.getProperty("java.io.tmpdir")).toAbsolutePath();
        Path abs = Paths.get(tmpDir.toUri());
        check(abs.isAbsolute(), "get(URI) absolute");
        check(abs.equals(tmpDir), "get(URI) round-trips an absolute path");
        if (java.io.File.separatorChar == '/') {
            check("/tmp/x".equals(Paths.get(URI.create("file:///tmp/x")).toString()),
                  "get(URI) keeps a POSIX absolute path");
        }
    }

    // ===== deepened contract coverage (batch A9) =====

    // multi-segment joining + separator handling + embedded separators
    static void testJoinMatrix() {
        check(Paths.get("a", "b", "c", "d").getNameCount() == 4, "four segments");
        check(Paths.get("/a", "b").equals(Paths.get("/a/b")), "root segment joined");
        check(Paths.get("a", "").equals(Paths.get("a")), "empty trailing segment tolerated");
        check(Paths.get("a/b", "c").equals(Paths.get("a/b/c")), "embedded separator in segment");
        // The joined form uses the provider's own separator.
        check(Paths.get("a", "b").toString().equals("a" + java.io.File.separatorChar + "b"),
              "joined toString uses the platform separator");
        check(Paths.get("a", "b", "c").isAbsolute() == false, "relative join stays relative");
        // A leading slash is drive-relative on Windows, so take the absolute
        // base from the platform instead of assuming a POSIX root.
        Path absBase = Paths.get(System.getProperty("java.io.tmpdir")).toAbsolutePath();
        check(Paths.get(absBase.toString(), "x").isAbsolute(), "absolute first segment absolute");
        check(Paths.get("a/b/c").equals(Paths.get("a", "b", "c")), "single equals multi");
        check(Paths.get(absBase.getRoot().toString()).getNameCount() == 0, "root only");
        // JDK-11+ Path.of delegating factory mirrors Paths.get exactly
        check(Path.of("a", "b").equals(Paths.get("a", "b")), "Path.of mirrors Paths.get join");
        check(Path.of("/a", "b").equals(Paths.get("/a/b")), "Path.of root join");
        check(Path.of("").equals(Paths.get("")), "Path.of empty mirrors get empty");
        // NUL byte is rejected with a precise exception (pre-verified fact)
        try {
            Paths.get("a\0b");
            check(false, "NUL in path must throw InvalidPathException");
        } catch (java.nio.file.InvalidPathException e) {
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

    // URI conversion matrix (probe-confirmed exact types)
    static void testUriMatrix() {
        // Round-trip an absolute path the platform itself produces: file:///tmp
        // is drive-relative (not absolute) on Windows.
        Path tmp = Paths.get(System.getProperty("java.io.tmpdir")).toAbsolutePath();
        Path decoded = Paths.get(tmp.toUri());
        check(decoded.isAbsolute(), "absolute path's file URI decodes absolute");
        check(decoded.equals(tmp), "file URI round-trips an absolute path");
        Path spaced = tmp.resolve("a b");
        check(spaced.toUri().getRawPath().contains("a%20b"), "space escaped in toUri");
        check(Paths.get(spaced.toUri()).equals(spaced), "escape round-trip a b");
        // The POSIX-rooted forms are only meaningful where / is absolute.
        if (java.io.File.separatorChar == '/') {
            Path abs = Paths.get(URI.create("file:///tmp/x"));
            check(abs.isAbsolute(), "file:///tmp/x absolute");
            check("/tmp/x".equals(abs.toString()), "file:///tmp/x decodes to /tmp/x");
            check(Paths.get(URI.create("file:///a%20b")).equals(Paths.get("/a b")),
                  "file URI decodes %20");
            check(Paths.get(URI.create("file:///a%20b")).toUri()
                      .equals(URI.create("file:///a%20b")),
                  "escape round-trip a b");
            check(Paths.get(URI.create("file:///")).equals(Paths.get("/")), "file:/// -> root");
            check(Paths.get(URI.create("file:///a/b")).equals(
                    java.nio.file.FileSystems.getDefault().getPath("/a/b")),
                  "get(URI) equals default fs getPath");
        }
        check(Paths.get(tmp.toUri()).getFileSystem()
              == java.nio.file.FileSystems.getDefault(), "get(URI) default provider");
        check(Path.of(URI.create("file:///a/b")).equals(Paths.get(URI.create("file:///a/b"))),
              "Path.of(URI) mirrors Paths.get(URI) (JDK-11+)");
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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testJoinMatrix();
        testUriMatrix();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
