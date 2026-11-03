/*
 * @test
 * @summary java.nio.file.PathMatcher conformance: white-box structure + black-box behavior.
 * @run main PathMatcherTest
 */

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;

public class PathMatcherTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
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

        // unknown syntax is rejected
        boolean iae = false;
        try { fs.getPathMatcher("jck-pattern:*.java"); }
        catch (IllegalArgumentException e) { iae = true; }
        catch (UnsupportedOperationException e) { iae = true; }
        check(iae, "unknown syntax -> IllegalArgumentException/UnsupportedOperationException");

        // malformed patterns rejected at creation
        boolean badRegex = false;
        try { fs.getPathMatcher("regex:[unclosed"); }
        catch (IllegalArgumentException e) { badRegex = true; }
        check(badRegex, "malformed regex -> IllegalArgumentException");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
