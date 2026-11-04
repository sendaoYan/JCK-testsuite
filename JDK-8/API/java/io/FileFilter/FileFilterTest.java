/*
 * @test
 * @summary java.io.FileFilter conformance (pure black-box): functional interface
 *          used with File.listFiles(FileFilter); filtering by name/extension/
 *          visibility; combination with logical operators; empty directory and
 *          no-match results.
 * @run main FileFilterTest
 */

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

public class FileFilterTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static File newTempDir() throws Exception {
        File d = File.createTempFile("jck-ff", ".tmp");
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
        touch("a.txt");
        touch("b.txt");
        touch("c.log");
        touch("hidden");
        new File(dir, "subdir").mkdir();
    }

    static void touch(String name) throws IOException {
        new File(dir, name).createNewFile();
    }

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
            deleteRecursively(dir);
        }
    }

    static boolean allEndWith(File[] files, String suffix) {
        for (File f : files) if (!f.getName().endsWith(suffix)) return false;
        return true;
    }

    public static void main(String[] args) throws Exception {
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
