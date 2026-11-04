/*
 * @test
 * @summary java.util.InvalidPropertiesFormatException API conformance: constructors and inheritance.
 * @run main InvalidPropertiesFormatExceptionTest
 */

import java.io.IOException;
import java.util.InvalidPropertiesFormatException;

public class InvalidPropertiesFormatExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testInvalidPropertiesFormatException() {
        InvalidPropertiesFormatException e = new InvalidPropertiesFormatException("m");
        check(e instanceof IOException, "extends IOException");
        check("m".equals(e.getMessage()), "message from (String) ctor");

        RuntimeException cause = new RuntimeException();
        InvalidPropertiesFormatException e2 = new InvalidPropertiesFormatException(cause);
        check(e2.getCause() == cause, "(Throwable) ctor sets cause");
    }

    public static void main(String[] args) {
        testInvalidPropertiesFormatException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
