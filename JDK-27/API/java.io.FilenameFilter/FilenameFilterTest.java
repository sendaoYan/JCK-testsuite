/*
 * @test
 * @summary java.io.FilenameFilter conformance: white-box structure + black-box behavior
 *          (functional interface used with File.list(FilenameFilter) and
 *          File.listFiles(FilenameFilter): dir/name pair contract, prefix/extension
 *          filters, composition by hand, no-match empty results, exception propagation).
 * @run main FilenameFilterTest
 */

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

public class FilenameFilterTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static File dir;

    static void setup() throws IOException {
        dir = java.nio.file.Files.createTempDirectory("fnfilter").toFile();
        touch("alpha.txt");
        touch("beta.txt");
        touch("gamma.dat");
        new File(dir, "nested").mkdir();
    }

    static void touch(String name) throws IOException {
        new File(dir, name).createNewFile();
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.io.FilenameFilter");
        check(c.isInterface(), "is an interface");
        Method m = c.getMethod("accept", File.class, String.class);
        check(Modifier.isAbstract(m.getModifiers()), "accept(File,String) abstract");
        check(m.getReturnType() == boolean.class, "accept returns boolean");
        check(File.class.getMethod("list", c) != null, "File.list(FilenameFilter) exists");
        check(File.class.getMethod("listFiles", c) != null,
              "File.listFiles(FilenameFilter) exists");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        setup();
        try {
            // 1. extension filter with String[] list()
            FilenameFilter txt = (d, name) -> name.endsWith(".txt");
            String[] names = dir.list(txt);
            check(names != null && names.length == 2, "two .txt names");
            check(Arrays.stream(names).allMatch(n -> n.endsWith(".txt")), "all names filtered");

            // 2. same filter through listFiles(): File objects returned
            File[] files = dir.listFiles(txt);
            check(files != null && files.length == 2
                  && Arrays.stream(files).allMatch(File::isFile),
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
            for (File f : dir.listFiles()) f.delete();
            dir.delete();
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
