/*
 * @test
 * @summary java.io.FileFilter conformance: white-box structure + black-box behavior
 *          (functional interface used with File.listFiles(FileFilter) and
 *          File.listFilesFiles(FileFilter); filtering by name/extension/visibility;
 *          combination with logical operators; empty directory and no-match results).
 * @run main FileFilterTest
 */

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class FileFilterTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static File dir;

    static void setup() throws IOException {
        dir = java.nio.file.Files.createTempDirectory("filefilter").toFile();
        touch("a.txt");
        touch("b.txt");
        touch("c.log");
        touch("hidden");
        new File(dir, "subdir").mkdir();
    }

    static void touch(String name) throws IOException {
        new File(dir, name).createNewFile();
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.io.FileFilter");
        check(c.isInterface(), "is an interface");
        Method m = c.getMethod("accept", File.class);
        check(Modifier.isAbstract(m.getModifiers()), "accept(File) abstract");
        check(m.getReturnType() == boolean.class, "accept returns boolean");
        check(File.class.getMethod("listFiles", c) != null,
              "File.listFiles(FileFilter) exists");
        check(!java.util.function.Predicate.class.isAssignableFrom(c),
              "FileFilter is NOT a Predicate (no combinators of its own)");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        setup();
        try {
            // 1. extension filter as a lambda
            FileFilter txt = f -> f.getName().endsWith(".txt");
            File[] txts = dir.listFiles(txt);
            check(txts != null && txts.length == 2, "two .txt files");
            check(allEndWith(txts, ".txt"), "filter honored for every result");

            // 2. directories-only filter excludes plain files
            FileFilter dirs = File::isDirectory;
            File[] only = dir.listFiles(dirs);
            check(only != null && only.length == 1 && only[0].getName().equals("subdir"),
                  "directories-only filter");

            // 3. composition by hand: negate / and / or written out
            FileFilter notTxt = f -> !txt.accept(f);
            check(notTxt.accept(new File(dir, "c.log")), "negation inverts");
            check(!notTxt.accept(new File(dir, "a.txt")), "negation inverts both ways");
            FileFilter txtAndFile = f -> f.isFile() && txt.accept(f);
            File[] both = dir.listFiles(txtAndFile);
            check(both != null && both.length == 2, "and-composition filters");
            FileFilter any = f -> txt.accept(f) || f.getName().endsWith(".log");
            File[] anyHits = dir.listFiles(any);
            check(anyHits != null && anyHits.length == 3, "or-composition filters");

            // 4. accept receives File objects rooted at the directory
            File[] rooted = dir.listFiles(f -> f.getParentFile() != null);
            check(rooted != null && rooted.length == 5, "parent set for listed entries");

            // 5. no match -> empty array (not null, not exception)
            File[] none = dir.listFiles(f -> false);
            check(none != null && none.length == 0, "no match yields empty array");

            // 6. accept-everything returns all non-directory entries plus subdirs
            File[] all = dir.listFiles(f -> true);
            check(all != null && all.length == 5, "accept-all returns every entry");

            // 7. used directly on File objects (no directory listing involved)
            FileFilter isHiddenNamed = f -> f.getName().equals("hidden");
            check(isHiddenNamed.accept(new File(dir, "hidden")), "direct accept true");
            check(!isHiddenNamed.accept(new File(dir, "a.txt")), "direct accept false");

            // 8. a filter that throws propagates from listFiles
            FileFilter bomb = f -> { throw new IllegalStateException("nope"); };
            try {
                dir.listFiles(bomb);
                check(false, "filter exception must propagate");
            } catch (IllegalStateException expected) {
                passed++;
            }
        } finally {
            for (File f : dir.listFiles()) f.delete();
            dir.delete();
        }
    }

    static boolean allEndWith(File[] files, String suffix) {
        for (File f : files) if (!f.getName().endsWith(suffix)) return false;
        return true;
    }

    // ================= 深化追加（契约级） =================

    static File freshDir() throws Exception {
        File d = File.createTempFile("jck-ff2", ".tmp");
        if (!d.delete() || !d.mkdir()) throw new AssertionError("fresh temp dir");
        return d;
    }

    static void touchIn(File dir, String name) throws IOException {
        if (!new File(dir, name).createNewFile()) throw new AssertionError("touch " + name);
    }

    // classic pre-lambda implementation style must keep working
    static class SuffixFilter implements FileFilter {
        private final String suffix;
        private int calls;
        SuffixFilter(String suffix) { this.suffix = suffix; }
        public boolean accept(File pathname) { calls++; return pathname.getName().endsWith(suffix); }
    }

    // ---- 9. invocation counting: accept is offered each entry exactly once ----
    static void testInvocationCounting() throws Exception {
        File d = freshDir();
        try {
            touchIn(d, "a.txt");
            touchIn(d, "b.txt");
            touchIn(d, "c.log");
            new File(d, "sub").mkdir();
            SuffixFilter f = new SuffixFilter(".txt");
            File[] got = d.listFiles(f);
            check(f.calls == 4, "accept offered once per entry (files and dirs)");
            check(got.length == 2, "only matching entries returned");
            // filtering again is deterministic and does not double-count state
            File[] again = d.listFiles(f);
            check(f.calls == 8, "second listing offers every entry again");
            check(again.length == 2, "second pass same result size");
            java.util.Arrays.sort(again, (x, y) -> x.getName().compareTo(y.getName()));
            check("a.txt".equals(again[0].getName()) && "b.txt".equals(again[1].getName()),
                  "identical membership on repeat");
        } finally {
            for (File k : d.listFiles()) k.delete();
            d.delete();
        }
    }

    // ---- 10. listFiles(FileFilter) is flat: nested entries are never offered ----
    static void testFlatNoRecursion() throws Exception {
        File d = freshDir();
        try {
            touchIn(d, "top.txt");
            File sub = new File(d, "sub");
            if (!sub.mkdir()) throw new AssertionError("mkdir sub");
            touchIn(sub, "deep.txt");
            final java.util.List<String> seen = new java.util.ArrayList<String>();
            File[] got = d.listFiles(x -> { seen.add(x.getName()); return true; });
            check(got.length == 2, "flat listing returns direct children only");
            check(seen.size() == 2 && !seen.contains("deep.txt"),
                  "filter never sees entries below the top level");
        } finally {
            new File(d, "sub/deep.txt").delete();
            new File(d, "sub").delete();
            for (File k : d.listFiles()) k.delete();
            d.delete();
        }
    }

    // ---- 11. null filter is equivalent to no filter ----
    static void testNullFilterEquivalence() throws Exception {
        File d = freshDir();
        try {
            touchIn(d, "x");
            touchIn(d, "y");
            check(d.listFiles((FileFilter) null).length == d.listFiles().length,
                  "null FileFilter lists everything");
        } finally {
            for (File k : d.listFiles()) k.delete();
            d.delete();
        }
    }

    // ---- 12. non-directory targets yield null (never an exception) ----
    static void testNullResults() throws Exception {
        File d = freshDir();
        try {
            touchIn(d, "plain");
            FileFilter t = f -> true;
            check(d.listFiles(t) != null && d.listFiles(t).length == 1, "dir listing works");
            check(new File(d, "plain").listFiles(t) == null, "plain file yields null");
            check(new File(d, "missing").listFiles(t) == null, "missing dir yields null");
            File empty = new File(d, "empty");
            check(empty.mkdir(), "mkdir empty");
            File[] none = empty.listFiles(t);
            check(none != null && none.length == 0, "empty dir yields length-0 array");
        } finally {
            new File(d, "empty").delete();
            for (File k : d.listFiles()) k.delete();
            d.delete();
        }
    }

    // ---- 13. anonymous-class and method-reference implementations ----
    static void testClassicImplementations() throws Exception {
        File d = freshDir();
        try {
            touchIn(d, "k.log");
            touchIn(d, "m.txt");
            FileFilter anon = new FileFilter() {
                public boolean accept(File pathname) { return pathname.getName().endsWith(".log"); }
            };
            File[] logs = d.listFiles(anon);
            check(logs.length == 1 && "k.log".equals(logs[0].getName()), "anonymous class filter");
            FileFilter isFile = File::isFile;
            check(d.listFiles(isFile).length == 2, "method-reference filter");
            FileFilter isDir = File::isDirectory;
            check(d.listFiles(isDir).length == 0, "no subdirectories here");
        } finally {
            for (File k : d.listFiles()) k.delete();
            d.delete();
        }
    }

    // ---- 14. interface shape: @FunctionalInterface, single abstract method ----
    static void testFunctionalInterfaceShape() throws Exception {
        Class<?> c = Class.forName("java.io.FileFilter");
        check(c.isAnnotationPresent(java.lang.FunctionalInterface.class),
              "@FunctionalInterface present");
        int abstractCount = 0;
        for (java.lang.reflect.Method m : c.getMethods())
            if (java.lang.reflect.Modifier.isAbstract(m.getModifiers())
                    && m.getDeclaringClass() == c) abstractCount++;
        check(abstractCount == 1, "exactly one abstract method of its own");
    }

    // ---- 15. filter exceptions propagate with the original exception object ----
    static void testExceptionIdentity() throws Exception {
        File d = freshDir();
        try {
            touchIn(d, "one");
            final IllegalStateException bomb = new IllegalStateException("boom-ff");
            try {
                d.listFiles(f -> { throw bomb; });
                check(false, "filter exception must propagate");
            } catch (IllegalStateException e) {
                check(e == bomb, "the very same exception instance propagates");
            }
        } finally {
            for (File k : d.listFiles()) k.delete();
            d.delete();
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testInvocationCounting();
        testFlatNoRecursion();
        testNullFilterEquivalence();
        testNullResults();
        testClassicImplementations();
        testFunctionalInterfaceShape();
        testExceptionIdentity();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
