/*
 * @test
 * @summary java.util.zip.DataFormatException conformance: constructors/inheritance.
 * @run main DataFormatExceptionTest
 */

import java.util.zip.DataFormatException;

public class DataFormatExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testDataFormatException() {
        DataFormatException e = new DataFormatException();
        check(e.getMessage() == null, "no-arg message null");
        DataFormatException e2 = new DataFormatException("bad format");
        check("bad format".equals(e2.getMessage()), "message ctor");
        check(e instanceof Exception, "is an Exception");
    }

    public static void main(String[] args) {
        testDataFormatException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
