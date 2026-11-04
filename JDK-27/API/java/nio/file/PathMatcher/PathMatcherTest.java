/*
 * @test
 * @summary java.nio.file.PathMatcher conformance: white-box structure + black-box behavior.
 * @run main PathMatcherTest
 */

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;

public class PathMatcherTest {
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

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.file.PathMatcher");
        check(c.isInterface(), "PathMatcher is an interface");
        check(java.util.Arrays.stream(c.getMethods())
              .anyMatch(m -> m.getName().equals("matches")), "has matches(Path)");
        check(c.isAnnotationPresent(FunctionalInterface.class)
              || c.getDeclaredMethods().length == 1, "functional interface");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        FileSystem fs = FileSystems.getDefault();

        // custom matcher via lambda
        PathMatcher exact = p -> p.getFileName() != null
                && p.getFileName().toString().equals("Main.java");
        check(exact.matches(Path.of("src/Main.java")), "custom matcher accepts");
        check(!exact.matches(Path.of("src/Other.java")), "custom matcher rejects");

        // glob syntax: exact-name matching, star, question mark, brace alternation
        PathMatcher glob = fs.getPathMatcher("glob:*.java");
        check(glob.matches(Path.of("Hello.java")), "glob *.java matches Hello.java");
        check(!glob.matches(Path.of("dir/Hello.java")), "glob *.java does not cross '/'");
        check(!glob.matches(Path.of("Hello.txt")), "glob rejects wrong extension");
        PathMatcher q = fs.getPathMatcher("glob:file?.txt");
        check(q.matches(Path.of("file1.txt")), "glob ? matches one char");
        check(!q.matches(Path.of("file12.txt")), "glob ? is exactly one char");
        PathMatcher braces = fs.getPathMatcher("glob:*.{java,txt}");
        check(braces.matches(Path.of("a.java")) && braces.matches(Path.of("b.txt")),
              "brace alternation");
        PathMatcher klass = fs.getPathMatcher("glob:[A-Z]*");
        check(klass.matches(Path.of("Abc")), "char class matches");
        check(!klass.matches(Path.of("abc")), "char class rejects lowercase");

        // regex syntax behaves as java.util.regex against the whole path string
        PathMatcher regex = fs.getPathMatcher("regex:.*\\.java");
        check(regex.matches(Path.of("any/dir/X.java")), "regex crosses separators");
        check(!regex.matches(Path.of("X.txt")), "regex rejects");
        PathMatcher anchored = fs.getPathMatcher("regex:^a$");
        check(anchored.matches(Path.of("a")), "anchored regex");

        // unknown syntax rejected (probe-confirmed: UnsupportedOperationException, not IAE)
        checkThrows(UnsupportedOperationException.class,
              () -> fs.getPathMatcher("jck-pattern:*.java"),
              "unknown syntax -> UnsupportedOperationException");
        // malformed regex pattern: PatternSyntaxException (an IllegalArgumentException subclass)
        checkThrows(java.util.regex.PatternSyntaxException.class,
              () -> fs.getPathMatcher("regex:[unclosed"),
              "malformed regex -> PatternSyntaxException");
        check(exact != fs.getPathMatcher("glob:*.java"), "custom matcher distinct from fs impl");
    }

    // ===== deepened contract coverage (batch A9): probe-confirmed matrices =====
    static void testGlobMatrix() {
        FileSystem fs = FileSystems.getDefault();
        PathMatcher star = fs.getPathMatcher("glob:*.java");
        check(!star.matches(Path.of("Hello.JAVA")), "glob case-sensitive");
        check(!star.matches(Path.of("Hellojava")), "glob dot is literal");
        check(star.matches(Path.of(".java")), "glob * matches empty");

        // ** crosses separators; */ does not
        PathMatcher dbStar = fs.getPathMatcher("glob:**.java");
        check(dbStar.matches(Path.of("dir/Hello.java")), "glob **.java crosses /");
        check(dbStar.matches(Path.of("Hello.java")), "glob **.java still matches flat");
        check(dbStar.matches(Path.of("a/b/c/X.java")), "glob **.java multi-level");
        PathMatcher dbSlash = fs.getPathMatcher("glob:**/*.java");
        check(dbSlash.matches(Path.of("a/b/C.java")), "glob **/*.java matches a/b/C.java");
        check(!dbSlash.matches(Path.of("C.java")), "glob **/*.java needs >=1 dir");
        check(dbSlash.matches(Path.of("d/C.java")), "glob **/*.java single dir");

        // [abc] and negated [!abc]
        PathMatcher cls = fs.getPathMatcher("glob:[abc].txt");
        check(cls.matches(Path.of("a.txt")) && cls.matches(Path.of("c.txt")),
              "glob [abc] matches members");
        check(!cls.matches(Path.of("d.txt")), "glob [abc] rejects others");
        check(!cls.matches(Path.of("ab.txt")), "glob [abc] single char");
        PathMatcher neg = fs.getPathMatcher("glob:[!abc].txt");
        check(neg.matches(Path.of("d.txt")), "glob [!abc] matches non-member");
        check(!neg.matches(Path.of("a.txt")), "glob [!abc] rejects member");

        // absolute-path glob
        PathMatcher absGl = fs.getPathMatcher("glob:/tmp/*.txt");
        check(absGl.matches(Path.of("/tmp/x.txt")), "absolute glob matches");
        check(!absGl.matches(Path.of("/var/x.txt")), "absolute glob rejects other root");
        check(!absGl.matches(Path.of("tmp/x.txt")), "absolute glob rejects relative");

        // empty pattern matches only the empty path
        PathMatcher emptyRe = fs.getPathMatcher("regex:");
        check(emptyRe.matches(Path.of("")), "empty regex matches empty path");
        check(!emptyRe.matches(Path.of("x")), "empty regex rejects non-empty");
        check(fs.getPathMatcher("glob:").matches(Path.of("")),
              "empty glob pattern matches empty path");
        checkThrows(IllegalArgumentException.class, () -> fs.getPathMatcher(":"),
              "colon-only (empty syntax) -> IllegalArgumentException");
    }

    static void testRegexMatrix() {
        FileSystem fs = FileSystems.getDefault();
        PathMatcher m = fs.getPathMatcher("regex:foo/bar/.*");
        check(m.matches(Path.of("foo/bar/x")), "regex matches deep");
        check(!m.matches(Path.of("foo/baz/x")), "regex anchored at start");
        // Path.of drops a trailing separator, so "foo/bar/" never reaches the matcher
        check(Path.of("foo/bar/").toString().equals("foo/bar"),
              "trailing separator normalized away");
        check(!m.matches(Path.of("foo/bar/")),
              "normalized path no longer satisfies trailing-/ regex");
        PathMatcher groups = fs.getPathMatcher("regex:(a|b)+c");
        check(groups.matches(Path.of("abaac")), "regex groups");
        check(!groups.matches(Path.of("c")), "regex groups require prefix");
        PathMatcher esc = fs.getPathMatcher("regex:a\\.b");
        check(esc.matches(Path.of("a.b")), "regex escaped dot literal");
        check(!esc.matches(Path.of("axb")), "regex escaped dot not wildcard");
        PathMatcher slashCls = fs.getPathMatcher("regex:[/]");
        check(slashCls.matches(Path.of("/")), "regex char class with separator");
    }

    static void testFactoryNegatives() {
        FileSystem fs = FileSystems.getDefault();
        // probe-confirmed exact types, identical on JDK-8 and JDK-27
        checkThrows(UnsupportedOperationException.class,
              () -> fs.getPathMatcher("jck:*.java"),
              "unknown syntax 'jck:' -> UnsupportedOperationException");
        checkThrows(NullPointerException.class, () -> fs.getPathMatcher(null),
              "getPathMatcher(null) NPE");
        checkThrows(IllegalArgumentException.class, () -> fs.getPathMatcher(""),
              "getPathMatcher(\"\") IAE (no syntax)");
        checkThrows(IllegalArgumentException.class, () -> fs.getPathMatcher("*.java"),
              "getPathMatcher(no colon) IAE");
        checkThrows(java.util.regex.PatternSyntaxException.class,
              () -> fs.getPathMatcher("regex:[unclosed"),
              "malformed regex -> PatternSyntaxException (IAE subclass)");
        checkThrows(java.util.regex.PatternSyntaxException.class,
              () -> fs.getPathMatcher("glob:{unclosed"),
              "malformed glob brace -> PatternSyntaxException (IAE subclass)");
        // matchers from the same pattern are independent objects, same behaviour
        check(fs.getPathMatcher("glob:*.java") != fs.getPathMatcher("glob:*.java"),
              "factory returns fresh instances");
        check(fs.getPathMatcher("glob:*.java").matches(Path.of("A.java")),
              "fresh instance equivalent");
        // Paths.get still interoperates with Path.of-obtained paths
        check(fs.getPathMatcher("glob:*.java").matches(Paths.get("B.java")),
              "matcher accepts Paths.get instance");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testGlobMatrix();
        testRegexMatrix();
        testFactoryNegatives();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
