/*
 * @test
 * @summary java.io.EOFException API conformance: extends IOException and the () and (String)
 *          constructors.
 * @run main EOFExceptionTest
 */

import java.io.EOFException;
import java.io.IOException;

public class EOFExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testEOFException() {
        EOFException e = new EOFException("msg");
        check(e instanceof IOException, "extends IOException");
        check("msg".equals(e.getMessage()), "(String) constructor");
        check(new EOFException().getMessage() == null, "() constructor null message");
    }

    public static void main(String[] args) {
        testEOFException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
