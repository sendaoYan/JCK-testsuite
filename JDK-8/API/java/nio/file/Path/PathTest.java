/*
 * @test
 * @summary java.nio.file.Path conformance: white-box structure (reflection) + black-box behavior
 *          (component access, resolution, relativize, File interop; JDK-8 has no Path.of).
 *          Platform-neutral: absolute paths are built from the filesystem root and
 *          separator is taken from File.separator, so Linux ("/a/b") and Windows
 *          ("C:\a\b") both pass.
 * @run main PathTest
 */

import java.lang.reflect.Modifier;
import java.net.URI;
import java.nio.file.FileSystems;
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

    // ---- platform-neutral path builders ----
    static final String SEP = java.io.File.separator;

    static Path root() {
        return FileSystems.getDefault().getRootDirectories().iterator().next();
    }

    static Path abs(String... names) {
        Path p = root();
        for (String n : names) p = p.resolve(n);
        return p;
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
        hasMethod(c, "getFileName");
        hasMethod(c, "getName", int.class);
        hasMethod(c, "subpath", int.class, int.class);
        hasMethod(c, "startsWith", String.class);
        hasMethod(c, "endsWith", String.class);
        hasMethod(c, "resolve", String.class);
        hasMethod(c, "resolveSibling", String.class);
        hasMethod(c, "relativize", Path.class);
        hasMethod(c, "normalize");
        hasMethod(c, "toUri");
        hasMethod(c, "toAbsolutePath");
        hasMethod(c, "toRealPath", java.nio.file.LinkOption[].class);
        hasMethod(c, "toFile");
        hasMethod(c, "register", java.nio.file.WatchService.class, java.nio.file.WatchEvent.Kind[].class);
    }

    // ---- black-box: behavior ----
    static void testBlackBox() {
        Path p = abs("a", "b", "c");
        check(p.isAbsolute(), "isAbsolute");
        check(root().equals(p.getRoot()), "getRoot");
        check("c".equals(p.getFileName().toString()), "getFileName");
        check(abs("a", "b").equals(p.getParent()), "getParent");
        check(p.getNameCount() == 3, "getNameCount");
        check("a".equals(p.getName(0).toString()), "getName(0)");
        check("b".equals(p.getName(1).toString()), "getName(1)");
        check(Paths.get("a", "b").equals(p.subpath(0, 2)), "subpath");
        check(p.startsWith(abs("a")), "startsWith Path");
        check(p.startsWith(abs("a").toString()), "startsWith String");
        check(p.endsWith(Paths.get("c")), "endsWith Path");
        check(p.endsWith("c"), "endsWith String");
        check(abs("a", "b", "c").equals(p.normalize()), "normalize");

        check(abs("a", "b", "c", "d").equals(p.resolve("d")), "resolve String");
        check(abs("a", "b", "d").equals(p.resolveSibling("d")), "resolveSibling String");
        check(abs("a", "b", "c").equals(abs("a", "b").resolve(Paths.get("c"))), "resolve Path");
        check(abs("a", "c").equals(abs("a", "b").resolveSibling(Paths.get("c"))), "resolveSibling Path");

        check(Paths.get("d").equals(abs("a", "b", "c").relativize(abs("a", "b", "c", "d"))), "relativize child");
        check(Paths.get("..", "x", "y").equals(abs("a", "b").relativize(abs("a", "x", "y"))), "relativize up");

        List<String> names = new ArrayList<>();
        for (Path n : p) names.add(n.toString());
        check(names.size() == 3 && names.get(0).equals("a") && names.get(2).equals("c"), "iterator");

        check(p.toAbsolutePath().isAbsolute(), "toAbsolutePath");
        check("file".equals(p.toUri().getScheme()), "toUri scheme is file");
        check(p.toFile().getName().equals("c"), "toFile");

        check(Paths.get(p.toUri()).equals(p), "Paths.get(URI) round-trip");

        check(p.equals(abs("a", "b", "c")) && p.hashCode() == abs("a", "b", "c").hashCode(), "equals/hashCode");
        check(p.compareTo(abs("a", "b", "c")) == 0, "compareTo equal");
        check(p.toString() != null, "toString non-null");

        boolean iae = false;
        try { p.subpath(0, 4); } catch (IllegalArgumentException e) { iae = true; }
        check(iae, "subpath out of bounds IAE");

        boolean iae2 = false;
        try { p.getName(-1); } catch (IllegalArgumentException e) { iae2 = true; }
        check(iae2, "getName negative IAE");
    }

    // ===== deepened contract coverage (batch A9) =====

    // getFileSystem / absolute-vs-relative component matrix / root path
    static void testComponentMatrix() {
        check(abs("a", "b", "c").getFileSystem() == FileSystems.getDefault(),
              "getFileSystem returns default FileSystem for default provider");
        check(Paths.get("a", "b").getFileSystem() == FileSystems.getDefault(),
              "getFileSystem of relative path is default FileSystem");

        // absolute a/b/c
        Path abs = abs("a", "b", "c");
        check(abs.isAbsolute(), "abs matrix isAbsolute");
        check(root().equals(abs.getRoot()), "abs root is the fs root");
        check("c".equals(abs.getFileName().toString()), "abs fileName c");
        check(abs("a", "b").equals(abs.getParent()), "abs parent a/b");
        check(abs.getNameCount() == 3, "abs nameCount 3");
        // relative a/b: no root, no absolute
        Path rel = Paths.get("a", "b");
        check(!rel.isAbsolute(), "rel not absolute");
        check(rel.getRoot() == null, "rel root null");
        check("b".equals(rel.getFileName().toString()), "rel fileName b");
        check(Paths.get("a").equals(rel.getParent()), "rel parent a");
        check(rel.getName(0).toString().equals("a") && rel.getName(1).toString().equals("b"),
              "rel names a,b");
        check(Paths.get("a", "b").getParent().getParent() == null, "a/b grandparent null");
        // root path itself
        Path root = root();
        check(root.isAbsolute(), "root isAbsolute");
        check(root.toString() != null && !root.toString().isEmpty(), "root toString non-empty");
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
        Path p = abs("a", "b", "c");
        check(Paths.get("a", "b", "c").equals(p.subpath(0, 3)), "subpath full");
        check(Paths.get("a").equals(p.subpath(0, 1)), "subpath first");
        check(Paths.get("c").equals(p.subpath(2, 3)), "subpath last");
        check(Paths.get("b", "c").equals(p.subpath(1, 3)), "subpath(1,3)");
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
        checkThrows(IllegalArgumentException.class, () -> root().subpath(0, 1),
              "subpath on root IAE");
        checkThrows(IllegalArgumentException.class, () -> p.getName(3),
              "getName(count) IAE");
        checkThrows(IllegalArgumentException.class, () -> p.getName(-1),
              "getName(-1) IAE");
    }

    // startsWith/endsWith: segment-wise semantics, not string prefix/suffix
    static void testStartsEndsWith() {
        Path p = abs("a", "bc");
        check(!p.startsWith(abs("a", "b")), "a/bc not startsWith a/b (segment compare)");
        check(Paths.get("a", "b").startsWith("a"), "a/b startsWith a");
        check(!Paths.get("a", "bc").startsWith("a/b"), "a/bc not startsWith a/b");
        check(p.startsWith(abs("a")), "a/bc startsWith a");
        check(p.startsWith(abs("a", "bc")), "startsWith equal path");
        check(p.startsWith(p), "startsWith itself");
        check(!p.startsWith("a"), "absolute not startsWith relative");
        check(!Paths.get("a", "b").startsWith(abs("a")), "relative not startsWith absolute (different roots)");
        check(!p.startsWith("b"), "not startsWith last segment");
        check(!p.startsWith(""), "not startsWith empty");
        check(!abs("a").startsWith(Paths.get("")), "not startsWith empty path");
        check(!p.endsWith(abs("a")), "a/bc not endsWith a");
        check(p.endsWith("bc"), "a/bc endsWith bc");
        check(!abs("a", "bc").endsWith(abs("bc")), "absolute suffix only matches equal path");
        check(!abs("a", "bc").endsWith("b"), "not endsWith b (partial segment)");
        check(p.endsWith(p), "endsWith itself");
        check(!p.endsWith(""), "not endsWith empty");
        check(Paths.get("").startsWith(Paths.get("")), "empty startsWith empty");
        check(p.endsWith("bc"), "endsWith last segment");
        // String and Path overloads agree (case sensitivity is provider-dependent and
        // is NOT asserted as a fixed direction here)
        check(abs("a", "b", "c").startsWith(abs("a", "b").toString())
                  == abs("a", "b", "c").startsWith(abs("a", "b")),
              "startsWith String/Path agree");
        check(abs("a", "b", "c").endsWith(Paths.get("b", "c").toString())
                  == abs("a", "b", "c").endsWith(Paths.get("b", "c")),
              "endsWith String/Path agree");
    }

    // normalize matrix (probe-confirmed values, built platform-neutrally)
    static void testNormalize() {
        check(Paths.get("a", "c").equals(Paths.get("a", "b", "..", "c").normalize()), "a/b/../c -> a/c");
        check(Paths.get("x").equals(Paths.get(".", "x").normalize()), "./x -> x");
        check(Paths.get("..").equals(Paths.get("a", "..", "..").normalize()), "a/../.. -> ..");
        check(abs("x").equals(root().resolve("..").resolve("x").normalize()), "/../x -> /x (root clamps)");
        check(Paths.get("..").equals(Paths.get("..").normalize()), ".. -> ..");
        check(Paths.get("a", "b").equals(Paths.get("a", ".", "b").normalize()), "a/./b -> a/b");
        check(abs("a").equals(abs("a", "b", "..").normalize()), "/a/b/.. -> /a");
        Path n = abs("a", "b", "c");
        check(n.normalize() == n, "normalize of already-normal path returns this (probe-confirmed)");
        Path dot = Paths.get(".", "a");
        Path dotOriginal = Paths.get(".", "a");
        check(Paths.get("a").equals(dot.normalize()), "./a -> a");
        check(Paths.get("..", "b").equals(Paths.get("a", "..", "..", "b").normalize()), "a/../../b -> ../b");
        // normalize does not mutate the receiver
        check(dot.equals(dotOriginal), "normalize leaves receiver intact");
    }

    // resolve / resolveSibling matrices
    static void testResolveResolveSibling() {
        Path p = abs("a", "b", "c");
        check(abs("a", "b", "c", "d").equals(p.resolve("d")), "resolve rel appended");
        check(p.resolve(abs("x")).equals(abs("x")), "resolve absolute yields argument path");
        check(p.resolve("").equals(p), "resolve(\"\") equals this");
        check(Paths.get("a", "b", "c").equals(Paths.get("a", "b").resolve("c")), "relative resolve");
        check(abs("c").equals(Paths.get("a", "b").resolve(abs("c"))), "relative base + absolute arg");
        check(abs("a", "b").equals(abs("a", "b").resolve(Paths.get(""))), "resolve empty Path equals this");
        checkThrows(NullPointerException.class, () -> p.resolve((String) null),
              "resolve((String)null) NPE");
        checkThrows(NullPointerException.class, () -> p.resolve((Path) null),
              "resolve((Path)null) NPE");

        check(abs("a", "b", "d").equals(p.resolveSibling("d")), "resolveSibling replaces last");
        check(abs("d").equals(abs("a").resolveSibling("d")), "resolveSibling at depth 1 -> root child");
        check(abs("x").equals(Paths.get("a", "b").resolveSibling(abs("x"))), "resolveSibling absolute arg");
        check(abs("a", "b").equals(p.resolveSibling("")), "resolveSibling(\"\") yields parent");
        check(Paths.get("a", "..").equals(Paths.get("a", "b").resolveSibling("..")), "resolveSibling .. kept literal");
        check(abs("a", "c").equals(abs("a", "b").resolveSibling(Paths.get("c"))),
              "resolveSibling Path overload");
        checkThrows(NullPointerException.class, () -> p.resolveSibling((String) null),
              "resolveSibling((String)null) NPE");
        checkThrows(NullPointerException.class, () -> p.resolveSibling((Path) null),
              "resolveSibling((Path)null) NPE");
        // resolve relativize round-trip on same-root absolutes
        Path a = abs("x", "y"), b = abs("x", "z");
        check(a.resolve(a.relativize(b)).normalize().equals(b), "resolve(relativize) round-trip");
    }

    // relativize matrix
    static void testRelativize() {
        check(Paths.get("..", "c").equals(abs("a", "b").relativize(abs("a", "c"))),
              "sibling target -> ../c");
        check(Paths.get("..").equals(Paths.get("a", "b").relativize(Paths.get("a"))),
              "parent target -> ..");
        check(Paths.get("a", "b").equals(root().relativize(abs("a", "b"))),
              "root -> deep yields downward path");
        check(Paths.get("..", "..").equals(abs("a", "b").relativize(root())),
              "a/b -> root yields ../..");
        check(Paths.get("..", "..", "..").equals(abs("a", "b", "c").relativize(root())),
              "a/b/c -> root yields ../../..");
        check(Paths.get("").equals(abs("a", "b").relativize(abs("a", "b"))),
              "same path -> empty");
        check(Paths.get("d").equals(abs("a", "b", "c").relativize(abs("a", "b", "c", "d"))),
              "child target -> d");
        check(Paths.get("..", "..", "..", "x").equals(Paths.get("a", "b").relativize(Paths.get("..", "x"))),
              "a/b vs ../x -> ../../../x");
        check(Paths.get("").equals(Paths.get("").relativize(Paths.get(""))), "empty vs empty -> empty");
        checkThrows(IllegalArgumentException.class,
              () -> abs("a", "b").relativize(Paths.get("a", "c")),
              "relativize absolute vs relative IAE");
        checkThrows(IllegalArgumentException.class,
              () -> Paths.get("a", "b").relativize(abs("a", "c")),
              "relativize relative vs absolute IAE");
        checkThrows(NullPointerException.class,
              () -> abs("a", "b").relativize(null), "relativize(null) NPE");
    }

    // compareTo ordering + consistency with equals
    static void testCompareToEquals() {
        check(Paths.get("a", "b").compareTo(Paths.get("a", "bc")) < 0, "prefix sorts before longer");
        check(Paths.get("a", "b").compareTo(Paths.get("a")) > 0, "longer sorts after prefix");
        check(Paths.get("a").compareTo(Paths.get("a", "b")) < 0, "a < a/b");
        check(abs("a", "b").compareTo(abs("a", "b")) == 0, "compareTo equal 0");
        check(abs("a", "b").compareTo(abs("a", "c")) < 0, "a/b < a/c");
        check(abs("a", "b").compareTo(abs("a", "a")) > 0, "a/b > a/a");
        check(Paths.get("").compareTo(Paths.get("")) == 0, "empty compareTo 0");
        checkThrows(NullPointerException.class,
              () -> abs("a").compareTo(null), "compareTo(null) NPE");
        // absolute vs relative are never equal
        check(!abs("a", "b").equals(Paths.get("a", "b")), "absolute != relative");
        check(!abs("a", "b").equals(null), "equals(null) false");
        check(!abs("a", "b").equals(abs("a", "b").toString()), "equals non-Path false");
        // sorted order over a TreeSet
        java.util.TreeSet<Path> set = new java.util.TreeSet<>(java.util.Arrays.asList(
                abs("a", "b"), abs("a"), abs("a", "bc"), abs("a", "b", "c")));
        java.util.Iterator<Path> it = set.iterator();
        List<Path> order = new ArrayList<>();
        while (it.hasNext()) order.add(it.next());
        // compareTo is provider-specific. The Windows provider compares the raw
        // path string case-insensitively with a '\\' separator, so 'bc' sorts
        // before 'b\\c' there ("C:\\a\\bc" < "C:\\a\\b\\c"), while the Unix
        // provider compares elements and puts the shorter "a/b" first.
        List<Path> expected;
        if (java.io.File.separatorChar == '\\') {
            expected = java.util.Arrays.asList(
                    abs("a"), abs("a", "b"), abs("a", "bc"), abs("a", "b", "c"));
        } else {
            expected = java.util.Arrays.asList(
                    abs("a"), abs("a", "b"), abs("a", "b", "c"), abs("a", "bc"));
        }
        check(order.equals(expected), "TreeSet follows the provider's compareTo order: " + order);
    }

    // iterator exact order and Iterable for-each
    static void testIteration() {
        List<String> names = new ArrayList<>();
        for (Path n : abs("a", "b", "c")) names.add(n.toString());
        check(names.size() == 3, "iteration count 3");
        check("a".equals(names.get(0)) && "b".equals(names.get(1)) && "c".equals(names.get(2)),
              "iteration order is root-to-leaf");
        List<String> rel = new ArrayList<>();
        for (Path n : Paths.get("x", "y")) rel.add(n.toString());
        check(rel.size() == 2 && rel.get(0).equals("x") && rel.get(1).equals("y"),
              "relative iteration order");
        List<String> empty = new ArrayList<>();
        for (Path n : Paths.get("")) empty.add(n.toString());
        check(empty.size() == 1 && empty.get(0).isEmpty(),
              "empty path iterates one empty name (probe-confirmed)");
        check(!root().iterator().hasNext(), "root path iterator empty");
    }

    // toUri round-trip (exact URI spelling is platform-specific, so round-trip is used)
    static void testUriConversion() throws Exception {
        Path nb = abs("a", "b");
        check("file".equals(nb.toUri().getScheme()), "toUri scheme file");
        check(nb.toUri().isAbsolute(), "toUri absolute for absolute path");
        check(Paths.get(nb.toUri()).equals(nb), "Paths.get(toUri) round-trip");
        Path spaced = abs("a b");
        check(Paths.get(spaced.toUri()).equals(spaced), "space percent-encoded round-trip");
        check(spaced.toUri().getPath().endsWith("a b"), "toUri decoded path keeps space");
        // existing directory gets a trailing slash
        Path tmpDir = Files.createTempDirectory("jck-path-uri");
        try {
            URI u = tmpDir.toUri();
            check(u.getPath().endsWith("/"), "toUri of existing directory ends with separator");
            check(u.isAbsolute(), "toUri absolute");
        } finally {
            Files.deleteIfExists(tmpDir);
        }
        // relative path resolves against cwd; toUri is absolute and keeps the tail
        Path r = Paths.get("rel", "x");
        check(r.toUri().isAbsolute(), "relative toUri absolutized");
        check(r.toUri().getPath().endsWith("rel/x"), "relative toUri keeps tail");
        checkThrows(NullPointerException.class, () -> Paths.get((URI) null), "Paths.get((URI)null) NPE");
    }

    // toAbsolutePath / toFile / File.toPath interop
    static void testAbsoluteFileConversion() {
        Path abs = abs("definitely", "not", "here");
        check(abs.toAbsolutePath().equals(abs), "absolute toAbsolutePath == this (equals)");
        Path rel = Paths.get("relpath");
        check(rel.toAbsolutePath().isAbsolute(), "relative toAbsolutePath is absolute");
        check(rel.toAbsolutePath().toString().endsWith("relpath"), "toAbsolutePath keeps tail");
        check(rel.toAbsolutePath().getRoot() != null, "toAbsolutePath rooted");
        check(!rel.toAbsolutePath().equals(rel), "toAbsolutePath differs from relative this");
        // File interop (build the File from a Path so the platform form agrees)
        java.io.File ftmp = root().resolve("tmp").toFile();
        check(ftmp.toPath().equals(root().resolve("tmp")), "File.toPath round-trip");
        check(root().resolve("tmp").toFile().equals(ftmp), "Path.toFile round-trip");
        Path rel2 = Paths.get("x", "y");
        check(rel2.toFile().getPath().equals(rel2.toString()), "relative Path.toFile path matches Path.toString");
        check(rel2.toFile().toPath().equals(rel2), "relative File round-trip");
        check(abs("a", "b").toFile().isAbsolute(), "absolute toFile isAbsolute");
        // toRealPath negative (exact type)
        checkThrows(java.nio.file.NoSuchFileException.class,
              () -> abs("no", "such", "jck", "ghost").toRealPath(),
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
        check(Paths.get("").equals(e.relativize(Paths.get(""))), "empty relativize empty");
        check(abs("a").resolve("").equals(abs("a")), "resolve empty unchanged");
    }

    // JDK-8 has no Path.of static factory (added in JDK 11)
    static void testJdk8NoPathOf() throws Exception {
        boolean absent = false;
        try {
            java.nio.file.Path.class.getMethod("of", String.class, String[].class);
        } catch (NoSuchMethodException e) { absent = true; }
        check(absent, "JDK-8 Path has no of(String,String...) (JDK-11+)");
        boolean absentUri = false;
        try {
            java.nio.file.Path.class.getMethod("of", URI.class);
        } catch (NoSuchMethodException e) { absentUri = true; }
        check(absentUri, "JDK-8 Path has no of(URI) (JDK-11+)");
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
        testJdk8NoPathOf();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
