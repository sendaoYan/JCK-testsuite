/*
 * @test
 * @summary java.time.DateTimeException API conformance: hierarchy and the (String)
 *          constructor.
 * @run main DateTimeExceptionTest
 */

import java.time.DateTimeException;

public class DateTimeExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testDateTimeException() {
        DateTimeException e = new DateTimeException("msg");
        check(e instanceof RuntimeException, "is a RuntimeException");
        check("msg".equals(e.getMessage()), "(String) constructor");
    }

    public static void main(String[] args) {
        testDateTimeException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
