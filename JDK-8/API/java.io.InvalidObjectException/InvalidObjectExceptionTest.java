/*
 * @test
 * @summary java.io.InvalidObjectException API conformance: constructors and inheritance.
 * @run main InvalidObjectExceptionTest
 */

import java.io.InvalidObjectException;
import java.io.ObjectStreamException;

public class InvalidObjectExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testInvalidObjectException() {
        InvalidObjectException e = new InvalidObjectException("m");
        check(e instanceof ObjectStreamException, "extends ObjectStreamException");
        check("m".equals(e.getMessage()), "message from (String) ctor");
    }

    public static void main(String[] args) {
        testInvalidObjectException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
