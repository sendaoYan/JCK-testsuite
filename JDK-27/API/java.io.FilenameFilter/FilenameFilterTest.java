/*
 * @test
 * @summary java.io.FilenameFilter API conformance: accept.
 * @run main FilenameFilterTest
 */

import java.io.File;
import java.io.FilenameFilter;

public class FilenameFilterTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testFilenameFilter() {
        FilenameFilter txt = (dir, name) -> name.endsWith(".txt");
        check(txt.accept(new File("."), "a.txt"), "accept matching");
        check(!txt.accept(new File("."), "a.java"), "reject non-matching");
    }

    public static void main(String[] args) {
        testFilenameFilter();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
