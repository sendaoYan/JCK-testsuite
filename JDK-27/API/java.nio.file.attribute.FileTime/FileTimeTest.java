/*
 * @test
 * @summary java.nio.file.attribute.FileTime conformance: millis/instant/compare.
 * @run main FileTimeTest
 */

import java.nio.file.attribute.FileTime;
import java.util.concurrent.TimeUnit;

public class FileTimeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testFileTime() {
        FileTime ft = FileTime.fromMillis(123456789L);
        check(ft.toMillis() == 123456789L, "toMillis");
        check(ft.to(TimeUnit.SECONDS) == 123456L, "to seconds");
        check(ft.toInstant() != null, "toInstant");
        check(ft.equals(FileTime.fromMillis(123456789L)), "equals");
        check(ft.compareTo(FileTime.fromMillis(0)) > 0, "compareTo");
        check(ft.toString() != null, "toString");
    }

    public static void main(String[] args) {
        testFileTime();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
