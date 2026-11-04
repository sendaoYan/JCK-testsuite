/*
 * @test
 * @summary java.util.zip.ZipError conformance: constructors/inheritance.
 * @run main ZipErrorTest
 */

import java.util.zip.ZipError;

public class ZipErrorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testZipError() {
        ZipError e = new ZipError("boom");
        check("boom".equals(e.getMessage()), "message ctor");
        check(e instanceof Error, "is an Error");
    }

    public static void main(String[] args) {
        testZipError();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
