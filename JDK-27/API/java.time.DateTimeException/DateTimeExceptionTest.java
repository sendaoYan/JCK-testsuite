/*
 * @test
 * @summary java.time.DateTimeException API conformance: unchecked hierarchy and invalid
 *          date rejection.
 * @run main DateTimeExceptionTest
 */

import java.time.DateTimeException;
import java.time.LocalDate;

public class DateTimeExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testDateTimeException() {
        DateTimeException e = new DateTimeException("bad");
        check("bad".equals(e.getMessage()), "message");
        check(e instanceof RuntimeException, "is unchecked RuntimeException");

        boolean thrown = false;
        try {
            LocalDate.of(2024, 2, 30);
        } catch (DateTimeException ex) {
            thrown = true;
        }
        check(thrown, "invalid date throws DateTimeException");
    }

    public static void main(String[] args) {
        testDateTimeException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
