/*
 * @test
 * @summary java.io.FilenameFilter conformance (pure black-box): functional
 *          interface used with File.list(FilenameFilter) and
 *          File.listFiles(FilenameFilter): dir/name pair contract, prefix/
 *          extension filters, composition by hand, no-match empty results,
 *          exception propagation.
 * @run main FilenameFilterTest
 */

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

public class FilenameFilterTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static File newTempDir() throws Exception {
        File d = File.createTempFile("jck-fnf", ".tmp");
        if (!d.delete() || !d.mkdir()) throw new AssertionError("temp dir");
        return d;
    }

    static void deleteRecursively(File f) {
        File[] kids = f.listFiles();
        if (kids != null) for (File c : kids) deleteRecursively(c);
        f.delete();
    }

    static File dir;

    static void setup() throws Exception {
        dir = newTempDir();
        touch("alpha.txt");
        touch("beta.txt");
        touch("gamma.dat");
        new File(dir, "nested").mkdir();
    }

    static void touch(String name) throws IOException {
        new File(dir, name).createNewFile();
    }

    static void testBlackBox() throws Exception {
        setup();
        try {
            // 1. extension filter with String[] list()
            FilenameFilter txt = (d, name) -> name.endsWith(".txt");
            String[] names = dir.list(txt);
            check(names != null && names.length == 2, "two .txt names");
            check(allMatch(names, ".txt"), "all names filtered");

            // 2. same filter through listFiles(): File objects returned
            File[] files = dir.listFiles(txt);
            check(files != null && files.length == 2 && allFiles(files),
                  "listFiles(FilenameFilter) returns matching files");

            // 3. the dir argument is the listed directory; name is the bare name
            final File[] seenDir = new File[1];
            final String[] seenName = new String[1];
            dir.listFiles((d, n) -> {
                if (n.equals("gamma.dat")) { seenDir[0] = d; seenName[0] = n; }
                return false;
            });
            check(seenDir[0] != null && seenDir[0].equals(dir)
                      && "gamma.dat".equals(seenName[0]),
                  "accept receives (dir, bareName) - name has no path separator");

            // 4. prefix filter and hand-written composition
            FilenameFilter startsA = (d, n) -> n.startsWith("a");
            check(dir.list(startsA).length == 1, "prefix filter");
            FilenameFilter aAndTxt = (d, n) -> startsA.accept(d, n) && txt.accept(d, n);
            check(dir.list(aAndTxt).length == 1, "and-composition");
            FilenameFilter aOrTxt = (d, n) -> startsA.accept(d, n) || txt.accept(d, n);
            check(dir.list(aOrTxt).length == 2, "or-composition");

            // 5. directories appear in listings too (filter must opt in)
            FilenameFilter all = (d, n) -> true;
            check(dir.list(all).length == 4, "accept-all sees files and the directory");

            // 6. no match -> empty arrays, never null
            String[] none = dir.list((d, n) -> false);
            check(none != null && none.length == 0, "String[] empty on no match");
            File[] noneF = dir.listFiles((d, n) -> false);
            check(noneF != null && noneF.length == 0, "File[] empty on no match");

            // 7. used directly without any listing
            FilenameFilter gamma = (d, n) -> n.equals("gamma.dat");
            check(gamma.accept(dir, "gamma.dat"), "direct accept true");
            check(!gamma.accept(dir, "beta.txt"), "direct accept false");

            // 8. a filter that throws propagates
            try {
                dir.list((d, n) -> { throw new IllegalStateException("nope"); });
                check(false, "filter exception must propagate");
            } catch (IllegalStateException expected) {
                passed++;
            }

            // 9. listing a nonexistent directory returns null (contract contrast)
            check(new File(dir, "missing").list(txt) == null, "null for a missing directory");
        } finally {
            deleteRecursively(dir);
        }
    }

    static boolean allMatch(String[] names, String suffix) {
        for (String n : names) if (!n.endsWith(suffix)) return false;
        return true;
    }

    static boolean allFiles(File[] files) {
        for (File f : files) if (!f.isFile()) return false;
        return true;
    }

    // ================= 深化追加（契约级） =================

    static File freshDir() throws Exception {
        File d = File.createTempFile("jck-fnf2", ".tmp");
        if (!d.delete() || !d.mkdir()) throw new AssertionError("fresh temp dir");
        return d;
    }

    static void touchIn(File dir, String name) throws IOException {
        if (!new File(dir, name).createNewFile()) throw new AssertionError("touch " + name);
    }

    static class ExtFilter implements FilenameFilter {
        private final String ext;
        private int calls;
        ExtFilter(String ext) { this.ext = ext; }
        public boolean accept(File dir, String name) { calls++; return name.endsWith(ext); }
    }

    // ---- 10. every accept call receives (listedDir, bareName) ----
    static void testDirAndNameContract() throws Exception {
        File d = freshDir();
        try {
            touchIn(d, "a.txt");
            touchIn(d, "b.txt");
            touchIn(d, "c.log");
            File sub = new File(d, "nested");
            if (!sub.mkdir()) throw new AssertionError("mkdir nested");
            final java.util.List<File> dirs = new java.util.ArrayList<File>();
            final java.util.List<String> names = new java.util.ArrayList<String>();
            d.listFiles((dir, name) -> { dirs.add(dir); names.add(name); return false; });
            check(dirs.size() == 4 && names.size() == 4, "one call per entry");
            for (File seenDir : dirs) check(seenDir.equals(d), "dir argument is always the listed dir");
            java.util.Collections.sort(names);
            check(names.equals(java.util.Arrays.asList("a.txt", "b.txt", "c.log", "nested")),
                  "bare names of every entry offered");
            for (String n : names) {
                check(n.indexOf(File.separatorChar) < 0, "names carry no path separator");
                check(new File(d, n).exists(), "name resolves to a real child");
            }
        } finally {
            deleteRecursively(d);
        }
    }

    // ---- 11. invocation counting ----
    static void testInvocationCounting() throws Exception {
        File d = freshDir();
        try {
            touchIn(d, "a.txt");
            touchIn(d, "b.dat");
            ExtFilter f = new ExtFilter(".txt");
            String[] got = d.list(f);
            check(f.calls == 2, "each entry offered exactly once");
            check(got.length == 1 && "a.txt".equals(got[0]), "only matching names returned");
            d.list(f);
            check(f.calls == 4, "second listing offers entries again");
        } finally {
            deleteRecursively(d);
        }
    }

    // ---- 12. null filter == no filter ----
    static void testNullFilterEquivalence() throws Exception {
        File d = freshDir();
        try {
            touchIn(d, "x");
            touchIn(d, "y");
            check(d.list(null).length == d.list().length, "null FilenameFilter lists everything");
            check(d.listFiles((FilenameFilter) null).length == d.listFiles().length,
                  "null FilenameFilter on listFiles too");
        } finally {
            deleteRecursively(d);
        }
    }

    // ---- 13. non-directory targets yield null; empty dir yields len 0 ----
    static void testNullResults() throws Exception {
        File d = freshDir();
        try {
            touchIn(d, "plain");
            FilenameFilter t = (dir, name) -> true;
            check(new File(d, "plain").list(t) == null, "list on a plain file is null");
            check(new File(d, "plain").listFiles(t) == null, "listFiles on a plain file is null");
            check(new File(d, "missing").list(t) == null, "list on a missing dir is null");
            File empty = new File(d, "empty");
            check(empty.mkdir(), "mkdir empty");
            check(empty.list(t) != null && empty.list(t).length == 0, "empty dir: length-0 array");
        } finally {
            deleteRecursively(d);
        }
    }

    // ---- 14. classic pre-lambda class implementation ----
    static void testClassicImplementation() throws Exception {
        File d = freshDir();
        try {
            touchIn(d, "k.log");
            touchIn(d, "m.txt");
            FilenameFilter logsOnly = new FilenameFilter() {
                public boolean accept(File dir, String name) { return name.endsWith(".log"); }
            };
            String[] got = d.list(logsOnly);
            check(got.length == 1 && "k.log".equals(got[0]), "anonymous class filter works");
            File[] files = d.listFiles(logsOnly);
            check(files.length == 1 && files[0].isFile(), "same filter for listFiles");
        } finally {
            deleteRecursively(d);
        }
    }

    // ---- 15. interface shape ----
    static void testFunctionalInterfaceShape() throws Exception {
        Class<?> c = Class.forName("java.io.FilenameFilter");
        check(c.isAnnotationPresent(java.lang.FunctionalInterface.class),
              "@FunctionalInterface present");
        int abstractCount = 0;
        for (java.lang.reflect.Method m : c.getMethods())
            if (java.lang.reflect.Modifier.isAbstract(m.getModifiers())
                    && m.getDeclaringClass() == c) abstractCount++;
        check(abstractCount == 1, "exactly one abstract method of its own");
    }

    // ---- 16. String[] view and File[] view agree for the same filter ----
    static void testStringFileConsistency() throws Exception {
        File d = freshDir();
        try {
            touchIn(d, "a.txt");
            touchIn(d, "b.txt");
            touchIn(d, "c.log");
            FilenameFilter txt = (dir, name) -> name.endsWith(".txt");
            String[] names = d.list(txt);
            File[] files = d.listFiles(txt);
            java.util.Arrays.sort(names);
            java.util.Arrays.sort(files, (x, y) -> x.getName().compareTo(y.getName()));
            check(names.length == files.length, "both views have the same size");
            for (int i = 0; i < names.length; i++)
                check(names[i].equals(files[i].getName()), "view " + i + " agrees on the name");
            check(files[0].getParentFile().equals(d), "File view is rooted at the directory");
        } finally {
            deleteRecursively(d);
        }
    }

    // ---- 17. filter exceptions propagate with the original exception object ----
    static void testExceptionIdentity() throws Exception {
        File d = freshDir();
        try {
            touchIn(d, "one");
            final IllegalStateException bomb = new IllegalStateException("boom-fnf");
            try {
                d.list((dir, name) -> { throw bomb; });
                check(false, "filter exception must propagate");
            } catch (IllegalStateException e) {
                check(e == bomb, "the very same exception instance propagates");
            }
        } finally {
            deleteRecursively(d);
        }
    }

    public static void main(String[] args) throws Exception {
        testBlackBox();
        testDirAndNameContract();
        testInvocationCounting();
        testNullFilterEquivalence();
        testNullResults();
        testClassicImplementation();
        testFunctionalInterfaceShape();
        testStringFileConsistency();
        testExceptionIdentity();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
