/*
 * @test
 * @summary java.nio.file.Path conformance: white-box structure (reflection) + black-box behavior
 *          (component access, resolution, relativize, File interop).
 * @run main PathTest
 *
 * 黑盒设计案:
 * 1. 契约: 文件系统路径抽象，提供分量访问(getRoot/getFileName/getParent/getName/subpath)、判断(startsWith/endsWith/isAbsolute)与组合(normalize/resolve/resolveSibling/relativize)。
 * 2. 场景: Path.of(String,String...) 与 Path.of(URI) 构造 -> 分量访问 -> 组合操作。
 * 3. 组合: /a/b/c 的分量 -> resolve/resolveSibling/relativize -> toAbsolutePath -> toFile 往返。
 * 4. 不变量: equals/hashCode 按路径等价；iterator 遍历分量；Path.of 与 Paths.get 等价。
 * 5. 边界: subpath 越界抛 IllegalArgumentException；normalize 折叠冗余分隔符。
 */

import java.lang.reflect.Modifier;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class PathTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    /** Throwing action so lambdas may call methods that declare checked exceptions. */
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

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    static void hasMethod(Class<?> c, String name, Class<?>... params) {
        try { c.getMethod(name, params); }
        catch (NoSuchMethodException e) { throw new AssertionError("missing method " + name); }
        passed++;
    }

    // ---- white-box: structure via reflection ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.file.Path");
        check(c.isInterface(), "Path is an interface");
        check(Modifier.isAbstract(c.getModifiers()), "interface is abstract");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(hasInterface(c, "java.lang.Comparable"), "extends Comparable");
        check(hasInterface(c, "java.lang.Iterable"), "extends Iterable");
        check(hasInterface(c, "java.nio.file.Watchable"), "extends Watchable");
        hasMethod(c, "of", String.class, String[].class);
        hasMethod(c, "getFileName");
        hasMethod(c, "resolve", String.class);
        hasMethod(c, "relativize", Path.class);
        hasMethod(c, "toFile");
    }

    // ---- black-box: behavior ----
    static void testBlackBox() {
        Path p = Path.of("/a/b/c");
        check(p.isAbsolute(), "isAbsolute");
        check("/".equals(p.getRoot().toString()), "getRoot");
        check("c".equals(p.getFileName().toString()), "getFileName");
        check("/a/b".equals(p.getParent().toString()), "getParent");
        check(p.getNameCount() == 3, "getNameCount");
        check("a".equals(p.getName(0).toString()), "getName(0)");
        check("b".equals(p.getName(1).toString()), "getName(1)");
        check("a/b".equals(p.subpath(0, 2).toString()), "subpath");
        check(p.startsWith(Path.of("/a")), "startsWith");
        check(p.endsWith(Path.of("c")), "endsWith");
        check("/a/b/c".equals(p.normalize().toString()), "normalize");

        check("/a/b/c/d".equals(p.resolve("d").toString()), "resolve");
        check("/a/b/d".equals(p.resolveSibling("d").toString()), "resolveSibling");
        check("/a/b/c".equals(Path.of("/a/b").resolve(Path.of("c")).toString()), "resolve Path");

        check("/a/b".equals(Path.of("/a//b/./").normalize().toString()), "normalize folds");

        check("d".equals(Path.of("/a/b/c").relativize(Path.of("/a/b/c/d")).toString()), "relativize child");
        check("../x/y".equals(Path.of("/a/b").relativize(Path.of("/a/x/y")).toString()), "relativize up");

        List<String> names = new ArrayList<>();
        for (Path n : p) names.add(n.toString());
        check(names.size() == 3 && names.get(0).equals("a") && names.get(2).equals("c"), "iterator");

        check(p.toAbsolutePath().isAbsolute(), "toAbsolutePath");
        check(p.toFile().getName().equals("c"), "toFile");

        check(Path.of(URI.create("file:///a/b")).equals(Path.of("/a/b")), "Path.of(URI)");

        check(p.equals(Path.of("/a/b/c")) && p.hashCode() == Path.of("/a/b/c").hashCode(), "equals/hashCode");

        boolean iae = false;
        try { p.subpath(0, 4); } catch (IllegalArgumentException e) { iae = true; }
        check(iae, "subpath out of bounds IAE");
    }

    // ===== deepened contract coverage (batch A9) =====

    // getFileSystem / absolute-vs-relative component matrix / root path
    static void testComponentMatrix() {
        check(Paths.get("/a/b/c").getFileSystem() == java.nio.file.FileSystems.getDefault(),
              "getFileSystem returns default FileSystem for default provider");
        check(Paths.get("a/b").getFileSystem() == java.nio.file.FileSystems.getDefault(),
              "getFileSystem of relative path is default FileSystem");

        // absolute /a/b/c
        Path abs = Paths.get("/a/b/c");
        check(abs.isAbsolute(), "abs matrix isAbsolute");
        check("/".equals(abs.getRoot().toString()), "abs root is /");
        check("c".equals(abs.getFileName().toString()), "abs fileName c");
        check("/a/b".equals(abs.getParent().toString()), "abs parent /a/b");
        check(abs.getNameCount() == 3, "abs nameCount 3");
        // relative a/b: no root, no absolute
        Path rel = Paths.get("a/b");
        check(!rel.isAbsolute(), "rel not absolute");
        check(rel.getRoot() == null, "rel root null");
        check("b".equals(rel.getFileName().toString()), "rel fileName b");
        check("a".equals(rel.getParent().toString()), "rel parent a");
        check(rel.getName(0).toString().equals("a") && rel.getName(1).toString().equals("b"),
              "rel names a,b");
        check(Paths.get("a/b").getParent().getParent() == null, "a/b grandparent null");
        // root path itself
        Path root = Paths.get("/");
        check(root.isAbsolute(), "root isAbsolute");
        check("/".equals(root.toString()), "root toString /");
        check(root.getFileName() == null, "root getFileName null");
        check(root.getParent() == null, "root getParent null");
        check(root.getRoot().equals(root), "root getRoot is itself");
        check(root.getNameCount() == 0, "root nameCount 0");
        // single name relative path
        Path single = Paths.get("x");
        check(single.getParent() == null, "single name parent null");
        check("x".equals(single.getFileName().toString()), "single name is its own fileName");
        // getName returns a relative path even for absolute Path
        check(!abs.getName(0).isAbsolute(), "getName(0) of absolute path is relative segment");
        check(abs.getName(0).getParent() == null, "name element has no parent");
        check(abs.getNameCount() == abs.subpath(0, 3).getNameCount(), "subpath full equals count");
    }

    // subpath matrix + exact bounds exceptions (all IAE per javadoc, probe-confirmed)
    static void testSubpathAndBounds() {
        Path p = Paths.get("/a/b/c");
        check("a/b/c".equals(p.subpath(0, 3).toString()), "subpath full");
        check("a".equals(p.subpath(0, 1).toString()), "subpath first");
        check("c".equals(p.subpath(2, 3).toString()), "subpath last");
        check("b/c".equals(p.subpath(1, 3).toString()), "subpath(1,3)");
        check(!p.subpath(0, 3).isAbsolute(), "subpath result is relative");
        checkThrows(IllegalArgumentException.class, () -> p.subpath(0, 0),
              "subpath(0,0) empty range IAE");
        checkThrows(IllegalArgumentException.class, () -> p.subpath(3, 3),
              "subpath(3,3) empty at end IAE");
        checkThrows(IllegalArgumentException.class, () -> p.subpath(2, 1),
              "subpath(2,1) begin>end IAE");
        checkThrows(IllegalArgumentException.class, () -> p.subpath(0, 4),
              "subpath endIndex>count IAE");
        checkThrows(IllegalArgumentException.class, () -> p.subpath(-1, 2),
              "subpath negative begin IAE");
        checkThrows(IllegalArgumentException.class, () -> Paths.get("/").subpath(0, 1),
              "subpath on root IAE");
        checkThrows(IllegalArgumentException.class, () -> p.getName(3),
              "getName(count) IAE");
        checkThrows(IllegalArgumentException.class, () -> p.getName(-1),
              "getName(-1) IAE");
    }

    // startsWith/endsWith: segment-wise semantics, not string prefix/suffix
    static void testStartsEndsWith() {
        Path p = Paths.get("/a/bc");
        check(!p.startsWith("/a/b"), "/a/bc not startsWith /a/b (segment compare)");
        check(Paths.get("a/b").startsWith("a"), "a/b startsWith a");
        check(!Paths.get("a/bc").startsWith("a/b"), "a/bc not startsWith a/b");
        check(p.startsWith("/a"), "/a/bc startsWith /a");
        check(p.startsWith(Paths.get("/a/bc")), "startsWith equal path");
        check(p.startsWith(p), "startsWith itself");
        check(!p.startsWith("a"), "absolute not startsWith relative");
        check(!Paths.get("a/b").startsWith("/a"), "relative not startsWith absolute (different roots)");
        check(!p.startsWith("b"), "not startsWith last segment");
        check(!p.startsWith(""), "not startsWith empty");
        check(!Paths.get("/a").startsWith(Paths.get("")), "not startsWith empty path");
        check(!p.endsWith("/a"), "/a/bc not endsWith /a");
        check(p.endsWith("bc"), "/a/bc endsWith bc");
        check(!Paths.get("/a/bc").endsWith(Paths.get("/bc")), "absolute suffix only matches equal path");
        check(!Paths.get("/a/bc").endsWith("/b"), "not endsWith /b (partial segment)");
        check(p.endsWith(p), "endsWith itself");
        check(!p.endsWith(""), "not endsWith empty");
        check(Paths.get("").startsWith(Paths.get("")), "empty startsWith empty");
        check(p.endsWith("bc"), "endsWith last segment");
        // case sensitivity (Linux default provider)
        check(!Paths.get("/A").startsWith("/a"), "startsWith case-sensitive");
        check(!Paths.get("/a/B").endsWith("b"), "endsWith case-sensitive");
        // String and Path overloads agree
        check(Paths.get("/a/b/c").startsWith("/a/b") == Paths.get("/a/b/c").startsWith(Paths.get("/a/b")),
              "startsWith String/Path agree");
        check(Paths.get("/a/b/c").endsWith("b/c") == Paths.get("/a/b/c").endsWith(Paths.get("b/c")),
              "endsWith String/Path agree");
    }

    // normalize matrix (probe-confirmed values)
    static void testNormalize() {
        check("a/c".equals(Paths.get("a/b/../c").normalize().toString()), "a/b/../c -> a/c");
        check("x".equals(Paths.get("./x").normalize().toString()), "./x -> x");
        check("..".equals(Paths.get("a/../..").normalize().toString()), "a/../.. -> ..");
        check("/x".equals(Paths.get("/../x").normalize().toString()), "/../x -> /x (root clamps)");
        check("..".equals(Paths.get("..").normalize().toString()), ".. -> ..");
        check("a/b".equals(Paths.get("a/./b/").normalize().toString()), "a/./b/ -> a/b");
        check("/".equals(Paths.get("//").normalize().toString()), "// -> /");
        check("/a".equals(Paths.get("/a/b/..").normalize().toString()), "/a/b/.. -> /a");
        Path n = Paths.get("/a/b/c");
        check(n.normalize() == n, "normalize of already-normal path returns this (probe-confirmed)");
        check(Paths.get("/a//b/./").normalize().equals(Paths.get("/a/b")), "redundant separators folded");
        check(Paths.get("a/b/c/").normalize().equals(Paths.get("a/b/c")), "trailing separator dropped");
        Path dot = Paths.get("./a");
        check("a".equals(dot.normalize().toString()), "./a -> a");
        check(Paths.get("a/../../b").normalize().toString().equals("../b"), "a/../../b -> ../b");
        // normalize does not mutate the receiver
        check("./a".equals(dot.toString()), "normalize leaves receiver intact");
    }

    // resolve / resolveSibling matrices
    static void testResolveResolveSibling() {
        Path p = Paths.get("/a/b/c");
        check("/a/b/c/d".equals(p.resolve("d").toString()), "resolve rel appended");
        check(p.resolve("/x").equals(Paths.get("/x")), "resolve absolute yields argument path");
        check(p.resolve("").equals(p), "resolve(\"\") equals this");
        check(Paths.get("a/b").resolve("c").toString().equals("a/b/c"), "relative resolve");
        check(Paths.get("a/b").resolve("/c").toString().equals("/c"), "relative base + absolute arg");
        check(Paths.get("/a/b").resolve(Paths.get("")).toString().equals("/a/b"), "resolve empty Path equals this");
        checkThrows(NullPointerException.class, () -> p.resolve((String) null),
              "resolve((String)null) NPE");
        checkThrows(NullPointerException.class, () -> p.resolve((Path) null),
              "resolve((Path)null) NPE");

        check("/a/b/d".equals(p.resolveSibling("d").toString()), "resolveSibling replaces last");
        check("/d".equals(Paths.get("/a").resolveSibling("d").toString()), "resolveSibling at depth 1 -> root child");
        check("/x".equals(Paths.get("a/b").resolveSibling("/x").toString()), "resolveSibling absolute arg");
        check("/a/b".equals(p.resolveSibling("").toString()), "resolveSibling(\"\") yields parent");
        check("a/..".equals(Paths.get("a/b").resolveSibling("..").toString()), "resolveSibling .. kept literal");
        check("/a/c".equals(Paths.get("/a/b").resolveSibling(Paths.get("c")).toString()),
              "resolveSibling Path overload");
        checkThrows(NullPointerException.class, () -> p.resolveSibling((String) null),
              "resolveSibling((String)null) NPE");
        checkThrows(NullPointerException.class, () -> p.resolveSibling((Path) null),
              "resolveSibling((Path)null) NPE");
        // resolve relativize round-trip on same-root absolutes
        Path a = Paths.get("/x/y"), b = Paths.get("/x/z");
        check(a.resolve(a.relativize(b)).normalize().equals(b), "resolve(relativize) round-trip");
    }

    // relativize matrix
    static void testRelativize() {
        check("../c".equals(Paths.get("/a/b").relativize(Paths.get("/a/c")).toString()),
              "sibling target -> ../c");
        check("..".equals(Paths.get("a/b").relativize(Paths.get("a")).toString()),
              "parent target -> ..");
        check("a/b".equals(Paths.get("/").relativize(Paths.get("/a/b")).toString()),
              "root -> deep yields downward path");
        check("../..".equals(Paths.get("/a/b").relativize(Paths.get("/")).toString()),
              "/a/b -> root yields ../..");
        check("../../..".equals(Paths.get("/a/b/c").relativize(Paths.get("/")).toString()),
              "/a/b/c -> root yields ../../..");
        check("".equals(Paths.get("/a/b").relativize(Paths.get("/a/b")).toString()),
              "same path -> empty");
        check("d".equals(Paths.get("/a/b/c").relativize(Paths.get("/a/b/c/d")).toString()),
              "child target -> d");
        check("../../../x".equals(Paths.get("a/b").relativize(Paths.get("../x")).toString()),
              "a/b vs ../x -> ../../../x (pre-verified fact)");
        check("".equals(Paths.get("").relativize(Paths.get("")).toString()), "empty vs empty -> empty");
        checkThrows(IllegalArgumentException.class,
              () -> Paths.get("/a/b").relativize(Paths.get("a/c")),
              "relativize absolute vs relative IAE");
        checkThrows(IllegalArgumentException.class,
              () -> Paths.get("a/b").relativize(Paths.get("/a/c")),
              "relativize relative vs absolute IAE");
        checkThrows(NullPointerException.class,
              () -> Paths.get("/a/b").relativize(null), "relativize(null) NPE");
    }

    // compareTo ordering + consistency with equals
    static void testCompareToEquals() {
        check(Paths.get("/a/b").compareTo(Paths.get("/a/bc")) < 0, "prefix sorts before longer");
        check(Paths.get("/a/b").compareTo(Paths.get("/a")) > 0, "longer sorts after prefix");
        check(Paths.get("a").compareTo(Paths.get("a/b")) < 0, "a < a/b");
        check(Paths.get("/a/b").compareTo(Paths.get("/a/b")) == 0, "compareTo equal 0");
        // UnixPath compares the raw byte sequences: '/' (0x2F) < 'a' (0x61), so
        // absolute "/a/b" sorts BEFORE relative "a/b"
        check(Paths.get("/a/b").compareTo(Paths.get("a/b")) < 0,
              "byte-lexicographic: /a/b < a/b (probe-confirmed)");
        check(Paths.get("/a/b").compareTo(Paths.get("/a/c")) < 0, "/a/b < /a/c");
        check(Paths.get("/a/b").compareTo(Paths.get("/a/a")) > 0, "/a/b > /a/a");
        check(Paths.get("").compareTo(Paths.get("")) == 0, "empty compareTo 0");
        checkThrows(NullPointerException.class,
              () -> Paths.get("/a").compareTo(null), "compareTo(null) NPE");
        // consistency: compareTo==0 iff equals (incl. redundant separator forms)
        Path x1 = Paths.get("/a/b"), x2 = Paths.get("/a//b");
        check(x1.equals(x2), "equals folds redundant separators");
        check(x1.hashCode() == x2.hashCode(), "hashCode agrees with equals");
        check(x1.compareTo(x2) == 0, "compareTo agrees with equals");
        check(Paths.get("a/b").equals(Paths.get("a/b/")), "trailing separator folded in equals");
        check(!Paths.get("/a/b").equals(Paths.get("/A/B")), "equals case-sensitive");
        check(!Paths.get("/a/b").equals(Paths.get("a/b")), "absolute != relative");
        check(!Paths.get("/a/b").equals(null), "equals(null) false");
        check(!Paths.get("/a/b").equals("/a/b"), "equals non-Path false");
        // sorted order over a TreeSet
        java.util.TreeSet<Path> set = new java.util.TreeSet<>(java.util.Arrays.asList(
                Paths.get("/a/b"), Paths.get("/a"), Paths.get("/a/bc"), Paths.get("/a/b/c")));
        java.util.Iterator<Path> it = set.iterator();
        String first = it.next().toString(), second = it.next().toString(),
               third = it.next().toString(), fourth = it.next().toString();
        check("/a".equals(first) && "/a/b".equals(second) && "/a/b/c".equals(third)
              && "/a/bc".equals(fourth), "TreeSet sorted lexicographically");
    }

    // iterator exact order and Iterable for-each
    static void testIteration() {
        List<String> names = new ArrayList<>();
        for (Path n : Paths.get("/a/b/c")) names.add(n.toString());
        check(names.size() == 3, "iteration count 3");
        check("a".equals(names.get(0)) && "b".equals(names.get(1)) && "c".equals(names.get(2)),
              "iteration order is root-to-leaf");
        List<String> rel = new ArrayList<>();
        for (Path n : Paths.get("x/y")) rel.add(n.toString());
        check(rel.size() == 2 && rel.get(0).equals("x") && rel.get(1).equals("y"),
              "relative iteration order");
        List<String> empty = new ArrayList<>();
        for (Path n : Paths.get("")) empty.add(n.toString());
        check(empty.size() == 1 && empty.get(0).isEmpty(),
              "empty path iterates one empty name (probe-confirmed)");
        check(!Paths.get("/").iterator().hasNext(), "root path iterator empty");
    }

    // toUri exact format matrix (Linux)
    static void testUriConversion() throws Exception {
        Path nb = Paths.get("/a/b");           // non-existent: no trailing slash
        check("file".equals(nb.toUri().getScheme()), "toUri scheme file");
        check(nb.toUri().equals(URI.create("file:///a/b")), "toUri exact for non-existent");
        check(Paths.get("/a b").toUri().equals(URI.create("file:///a%20b")),
              "toUri percent-encodes space");
        check(Paths.get("/").toUri().equals(URI.create("file:///")), "root toUri file:///");
        check("/a%20b".equals(Paths.get("/a b").toUri().getRawPath()), "toUri raw path encoded");
        check("/a b".equals(Paths.get("/a b").toUri().getPath()), "toUri decoded path");
        check(Paths.get(Paths.get("/a b").toUri()).equals(Paths.get("/a b")),
              "Paths.get(toUri) round-trip");
        // existing directory gets a trailing slash
        Path tmpDir = Files.createTempDirectory("jck-path-uri");
        try {
            URI u = tmpDir.toUri();
            check(u.getPath().endsWith("/"), "toUri of existing directory ends with separator");
            check(u.isAbsolute(), "toUri absolute");
        } finally {
            Files.deleteIfExists(tmpDir);
        }
        // relative path is resolved against cwd first
        Path r = Paths.get("rel/x");
        check(r.toUri().isAbsolute(), "relative toUri absolutized");
        check(r.toUri().getPath().endsWith("rel/x"), "relative toUri keeps tail");
        check(r.toUri().getPath().startsWith("/"), "relative toUri rooted");
        checkThrows(NullPointerException.class, () -> Paths.get((URI) null), "Paths.get((URI)null) NPE");
    }

    // toAbsolutePath / toFile / File.toPath interop
    static void testAbsoluteFileConversion() {
        Path abs = Paths.get("/definitely/not/here");
        check(abs.toAbsolutePath().equals(abs), "absolute toAbsolutePath == this (equals)");
        Path rel = Paths.get("relpath");
        check(rel.toAbsolutePath().isAbsolute(), "relative toAbsolutePath is absolute");
        check(rel.toAbsolutePath().toString().endsWith("relpath"), "toAbsolutePath keeps tail");
        check(rel.toAbsolutePath().getRoot() != null, "toAbsolutePath rooted");
        check(!rel.toAbsolutePath().equals(rel), "toAbsolutePath differs from relative this");
        // File interop
        java.io.File f = new java.io.File("/tmp");
        check(f.toPath().equals(Paths.get("/tmp")), "File.toPath round-trip");
        check(Paths.get("/tmp").toFile().equals(f), "Path.toFile round-trip");
        check(Paths.get("x/y").toFile().getPath().equals("x/y"), "relative Path.toFile");
        check(Paths.get("x/y").toFile().toPath().equals(Paths.get("x/y")), "relative File round-trip");
        check(Paths.get("/a/b").toFile().isAbsolute(), "absolute toFile isAbsolute");
        // toRealPath negative (exact type)
        checkThrows(java.nio.file.NoSuchFileException.class,
              () -> Paths.get("/no/such/jck/ghost").toRealPath(),
              "toRealPath nonexistent NoSuchFileException");
    }

    // empty-path contract (probe-confirmed quirks)
    static void testEmptyPath() {
        Path e = Paths.get("");
        check("".equals(e.toString()), "empty toString");
        check(e.getNameCount() == 1, "empty path nameCount is 1");
        check(e.getFileName() != null && e.getFileName().toString().isEmpty(),
              "empty getFileName is the empty name");
        check(e.getParent() == null, "empty getParent null");
        check(e.getRoot() == null, "empty getRoot null");
        check(!e.isAbsolute(), "empty not absolute");
        check(e.equals(Paths.get("")), "empty equals empty");
        check(e.compareTo(Paths.get("")) == 0, "empty compareTo 0");
        check(e.startsWith(Paths.get("")), "empty startsWith empty");
        check("".equals(e.relativize(Paths.get("")).toString()), "empty relativize empty");
        check(Paths.get("/a").resolve("").equals(Paths.get("/a")), "resolve empty unchanged");
    }

    // JDK-11+ Path.of static factories: equivalence with Paths.get (JDK-27 only)
    static void testPathOf() {
        check(Path.of("/a/b/c").equals(Paths.get("/a/b/c")), "Path.of(String) == Paths.get");
        check(Path.of("a", "b", "c").equals(Paths.get("a/b/c")), "Path.of(String...) joins");
        check(Path.of(URI.create("file:///a/b")).equals(Paths.get("/a/b")), "Path.of(URI)");
        check(Path.of("").equals(Paths.get("")), "Path.of empty string");
        check(Path.of("a", "b").equals(Path.of("a/b")), "Path.of joined equals single");
        check(Path.of("x").isAbsolute() == false, "Path.of relative");
        checkThrows(NullPointerException.class, () -> Path.of((String) null),
              "Path.of((String)null) NPE");
        checkThrows(NullPointerException.class, () -> Path.of("a", (String[]) null),
              "Path.of(more null) NPE");
        checkThrows(NullPointerException.class, () -> Path.of((URI) null),
              "Path.of((URI)null) NPE");
        checkThrows(IllegalArgumentException.class,
              () -> Path.of(URI.create("file:a/b")), "Path.of non-hierarchical file URI IAE");
        check(Path.of("a/b/c").getName(1).equals(Path.of("b")), "Path.of segment access");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testComponentMatrix();
        testSubpathAndBounds();
        testStartsEndsWith();
        testNormalize();
        testResolveResolveSibling();
        testRelativize();
        testCompareToEquals();
        testIteration();
        testUriConversion();
        testAbsoluteFileConversion();
        testEmptyPath();
        testPathOf();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
