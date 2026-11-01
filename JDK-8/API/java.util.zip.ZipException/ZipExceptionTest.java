/*
 * @test
 * @summary java.util.zip.ZipException conformance: constructors/inheritance.
 * @run main ZipExceptionTest
 */

import java.io.IOException;
import java.util.zip.ZipException;

public class ZipExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testZipException() {
        ZipException e = new ZipException();
        check(e.getMessage() == null, "no-arg message null");
        ZipException e2 = new ZipException("bad zip");
        check("bad zip".equals(e2.getMessage()), "message ctor");
        check(e2 instanceof IOException, "is an IOException");
    }

    public static void main(String[] args) {
        testZipException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
