/*
 * @test
 * @summary java.io.FileFilter API conformance: accept.
 * @run main FileFilterTest
 */

import java.io.File;
import java.io.FileFilter;

public class FileFilterTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testFileFilter() {
        FileFilter txt = f -> f.getName().endsWith(".txt");
        check(txt.accept(new File("a.txt")), "accept matching");
        check(!txt.accept(new File("a.java")), "reject non-matching");
    }

    public static void main(String[] args) {
        testFileFilter();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
