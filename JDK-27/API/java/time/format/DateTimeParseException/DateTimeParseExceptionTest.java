/*
 * @test
 * @summary java.time.format.DateTimeParseException conformance: white-box structure + black-box behavior.
 * @run main DateTimeParseExceptionTest
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DateTimeParseExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.time.format.DateTimeParseException");
        check(!c.isInterface(), "not interface");
        check(!Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(!c.isEnum(), "not enum");
        check(c.getSuperclass() == DateTimeException.class, "extends DateTimeException");
        check(RuntimeException.class.isAssignableFrom(c), "is a RuntimeException");

        boolean has3 = false;
        boolean has4 = false;
        for (Constructor<?> k : c.getConstructors()) {
            Class<?>[] p = k.getParameterTypes();
            if (p.length == 3 && p[0] == String.class && p[1] == CharSequence.class
                    && p[2] == int.class) has3 = true;
            if (p.length == 4 && p[0] == String.class && p[1] == CharSequence.class
                    && p[2] == int.class && p[3] == Throwable.class) has4 = true;
        }
        check(has3, "constructor (String, CharSequence, int) present");
        check(has4, "constructor (String, CharSequence, int, Throwable) present");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        DateTimeParseException e = new DateTimeParseException("bad", "text", 0);
        check(e instanceof DateTimeException, "extends DateTimeException");
        check("bad".equals(e.getMessage()), "getMessage");
        check("text".equals(e.getParsedString()), "getParsedString");
        check(e.getErrorIndex() == 0, "getErrorIndex");

        Throwable cause = new IllegalArgumentException("root");
        DateTimeParseException e2 = new DateTimeParseException("bad", "text", 3, cause);
        check(e2.getErrorIndex() == 3, "getErrorIndex with cause");
        check(e2.getCause() == cause, "getCause");

        // Naturally produced by a strict ISO date parse of an out-of-range month.
        try {
            DateTimeFormatter.ISO_LOCAL_DATE.parse("2024-13-01", LocalDate::from);
            throw new AssertionError("parse invalid: expected DateTimeParseException");
        } catch (DateTimeParseException expected) {
            check("2024-13-01".equals(expected.getParsedString()), "thrown getParsedString");
            check(expected.getErrorIndex() >= 0, "thrown getErrorIndex");
            check(expected.getMessage() != null, "thrown getMessage non-null");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
