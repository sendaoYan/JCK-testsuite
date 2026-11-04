/*
 * @test
 * @summary java.time.temporal.UnsupportedTemporalTypeException conformance: message and hierarchy.
 * @run main UnsupportedTemporalTypeExceptionTest
 */

import java.time.DateTimeException;
import java.time.temporal.UnsupportedTemporalTypeException;

public class UnsupportedTemporalTypeExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testUnsupportedTemporalTypeException() {
        UnsupportedTemporalTypeException e = new UnsupportedTemporalTypeException("msg");
        check("msg".equals(e.getMessage()), "getMessage");
        check(e instanceof DateTimeException, "is DateTimeException");
    }

    public static void main(String[] args) {
        testUnsupportedTemporalTypeException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
