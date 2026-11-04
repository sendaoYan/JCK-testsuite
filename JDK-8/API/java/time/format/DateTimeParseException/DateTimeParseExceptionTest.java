/*
 * @test
 * @summary java.time.format.DateTimeParseException API conformance: extends DateTimeException,
 *          getMessage/getParsedString/getErrorIndex, and thrown by LocalDate.parse.
 * @run main DateTimeParseExceptionTest
 */

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class DateTimeParseExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testConstructors() {
        DateTimeParseException e = new DateTimeParseException("bad", "text", 0);
        check(e instanceof DateTimeException, "extends DateTimeException");
        check("bad".equals(e.getMessage()), "getMessage");
        check("text".equals(e.getParsedString()), "getParsedString");
        check(e.getErrorIndex() == 0, "getErrorIndex");
    }

    static void testThrownByParse() {
        try {
            LocalDate.parse("not-a-date");
            throw new AssertionError("parse invalid: expected DateTimeParseException");
        } catch (DateTimeParseException expected) {
            passed++;
        }
    }

    public static void main(String[] args) {
        testConstructors();
        testThrownByParse();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
