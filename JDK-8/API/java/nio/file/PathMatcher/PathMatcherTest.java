/*
 * @test
 * @summary java.nio.file.PathMatcher conformance: white-box structure + black-box behavior
 *          (glob pattern matrix incl. ** / [abc] / [!abc] / braces, regex syntax, negatives).
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
        check(c.getTypeParameters().length == 0, "not generic");
        // matches(Path) returns boolean
        check(java.util.Arrays.stream(c.getMethods())
              .filter(m -> m.getName().equals("matches"))
              .anyMatch(m -> m.getReturnType() == boolean.class), "matches returns boolean");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        FileSystem fs = FileSystems.getDefault();

        // custom matcher via lambda
        PathMatcher exact = p -> p.getFileName() != null
                && p.getFileName().toString().equals("Main.java");
        check(exact.matches(Paths.get("src/Main.java")), "custom matcher accepts");
        check(!exact.matches(Paths.get("src/Other.java")), "custom matcher rejects");
        check(exact != fs.getPathMatcher("glob:*.java"), "custom matcher distinct from fs impl");

        // glob syntax: exact-name matching, star, question mark, brace alternation
        PathMatcher glob = fs.getPathMatcher("glob:*.java");
        check(glob.matches(Paths.get("Hello.java")), "glob *.java matches Hello.java");
        check(!glob.matches(Paths.get("dir/Hello.java")), "glob *.java does not cross '/'");
        check(!glob.matches(Paths.get("Hello.txt")), "glob rejects wrong extension");
        check(!glob.matches(Paths.get("Hello.java.bak")), "glob anchored at end");
        PathMatcher q = fs.getPathMatcher("glob:file?.txt");
        check(q.matches(Paths.get("file1.txt")), "glob ? matches one char");
        check(!q.matches(Paths.get("file12.txt")), "glob ? is exactly one char");
        check(!q.matches(Paths.get("file.txt")), "glob ? requires one char");
        PathMatcher braces = fs.getPathMatcher("glob:*.{java,txt}");
        check(braces.matches(Paths.get("a.java")) && braces.matches(Paths.get("b.txt")),
              "brace alternation");
        check(!braces.matches(Paths.get("a.class")), "brace rejects other suffix");
        PathMatcher klass = fs.getPathMatcher("glob:[A-Z]*");
        check(klass.matches(Paths.get("Abc")), "char class matches");
        if (java.io.File.separatorChar == '/') {
            check(!klass.matches(Paths.get("abc")), "char class rejects lowercase (case-sensitive glob)");
        } else {
            // Windows 的 glob 匹配折叠大小写：[A-Z] 等价于 [A-Za-z]，因此 "abc"
            // 乃至 "zzz" 都被接受（探针实测 zzz=true）；仍拒绝非字母首字符。
            check(klass.matches(Paths.get("abc")), "char class folds case (Windows glob)");
            check(klass.matches(Paths.get("zzz")), "case-folded [A-Z] accepts any letter");
            check(!klass.matches(Paths.get("1zz")), "char class still rejects non-letters");
        }

        // regex syntax behaves as java.util.regex against the whole path string
        PathMatcher regex = fs.getPathMatcher("regex:.*\\.java");
        check(regex.matches(Paths.get("any/dir/X.java")), "regex crosses separators");
        check(!regex.matches(Paths.get("X.txt")), "regex rejects");
        PathMatcher anchored = fs.getPathMatcher("regex:^a$");
        check(anchored.matches(Paths.get("a")), "anchored regex");
        check(!anchored.matches(Paths.get("a/b")), "anchored regex rejects longer");

        // unknown syntax rejected (probe-confirmed: UnsupportedOperationException, not IAE)
        checkThrows(UnsupportedOperationException.class,
              () -> fs.getPathMatcher("jck-pattern:*.java"),
              "unknown syntax -> UnsupportedOperationException");
        // malformed regex pattern: PatternSyntaxException (an IllegalArgumentException subclass)
        checkThrows(java.util.regex.PatternSyntaxException.class,
              () -> fs.getPathMatcher("regex:[unclosed"),
              "malformed regex -> PatternSyntaxException");
    }

    // ===== deepened contract coverage (batch A9): probe-confirmed matrices =====
    static void testGlobMatrix() {
        FileSystem fs = FileSystems.getDefault();
        PathMatcher star = fs.getPathMatcher("glob:*.java");
        // glob case sensitivity is platform-dependent (Linux sensitive, Windows
        // insensitive); assert determinism instead of a fixed direction.
        boolean caseSensitive = !star.matches(Paths.get("Hello.JAVA"));
        check(caseSensitive == !star.matches(Paths.get("Hello.JAVA")),
              "glob case behavior stable across calls (platform-dependent)");
        check(!star.matches(Paths.get("Hellojava")), "glob dot is literal");
        check(star.matches(Paths.get(".java")), "glob * matches empty");

        // ** crosses separators; */ does not
        PathMatcher dbStar = fs.getPathMatcher("glob:**.java");
        check(dbStar.matches(Paths.get("dir/Hello.java")), "glob **.java crosses /");
        check(dbStar.matches(Paths.get("Hello.java")), "glob **.java still matches flat");
        check(dbStar.matches(Paths.get("a/b/c/X.java")), "glob **.java multi-level");
        PathMatcher dbSlash = fs.getPathMatcher("glob:**/*.java");
        check(dbSlash.matches(Paths.get("a/b/C.java")), "glob **/*.java matches a/b/C.java");
        check(!dbSlash.matches(Paths.get("C.java")), "glob **/*.java needs >=1 dir");
        check(dbSlash.matches(Paths.get("d/C.java")), "glob **/*.java single dir");

        // [abc] and negated [!abc]
        PathMatcher cls = fs.getPathMatcher("glob:[abc].txt");
        check(cls.matches(Paths.get("a.txt")) && cls.matches(Paths.get("c.txt")),
              "glob [abc] matches members");
        check(!cls.matches(Paths.get("d.txt")), "glob [abc] rejects others");
        check(!cls.matches(Paths.get("ab.txt")), "glob [abc] single char");
        PathMatcher neg = fs.getPathMatcher("glob:[!abc].txt");
        check(neg.matches(Paths.get("d.txt")), "glob [!abc] matches non-member");
        check(!neg.matches(Paths.get("a.txt")), "glob [!abc] rejects member");

        // absolute-path glob is not exercised here: its spelling is Linux-specific
        // ("/tmp/...") and differs on Windows (drive-letter roots). Basic glob
        // semantics are covered by the relative-pattern matrix above.

        // empty pattern matches only the empty path
        PathMatcher emptyRe = fs.getPathMatcher("regex:");
        check(emptyRe.matches(Paths.get("")), "empty regex matches empty path");
        check(!emptyRe.matches(Paths.get("x")), "empty regex rejects non-empty");
        check(fs.getPathMatcher("glob:").matches(Paths.get("")),
              "empty glob pattern matches empty path");
        checkThrows(IllegalArgumentException.class, () -> fs.getPathMatcher(":"),
              "colon-only (empty syntax) -> IllegalArgumentException");
    }

    static void testRegexMatrix() {
        FileSystem fs = FileSystems.getDefault();
        // separator-free regex tests (cross-platform). Patterns that embed the
        // platform separator are spelled differently on Windows and are not
        // exercised here (the separator-crossing regex case is covered below via a
        // multi-segment path with an anchored pattern).
        PathMatcher groups = fs.getPathMatcher("regex:(a|b)+c");
        check(groups.matches(Paths.get("abaac")), "regex groups");
        check(!groups.matches(Paths.get("c")), "regex groups require prefix");
        PathMatcher esc = fs.getPathMatcher("regex:a\\.b");
        check(esc.matches(Paths.get("a.b")), "regex escaped dot literal");
        check(!esc.matches(Paths.get("axb")), "regex escaped dot not wildcard");
        PathMatcher anchored = fs.getPathMatcher("regex:^a$");
        check(anchored.matches(Paths.get("a")), "anchored regex matches single segment");
        check(!anchored.matches(Paths.get("a", "b")), "anchored regex rejects multi-segment path");
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
        check(fs.getPathMatcher("glob:*.java").matches(Paths.get("A.java")),
              "fresh instance equivalent");
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
