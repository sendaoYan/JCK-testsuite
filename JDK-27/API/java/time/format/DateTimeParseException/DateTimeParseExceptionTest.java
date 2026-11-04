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
import java.time.Instant;
import java.time.LocalTime;

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
        testDeepDTPE();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ==== contract deepening harness (appended) ====
    interface DeepRun { void run() throws Throwable; }

    static void xact(DeepRun r, Class<?> type, String exactMsg, String tag) {
        try {
            r.run();
            throw new AssertionError(tag + ": expected " + type.getSimpleName() + " but nothing was thrown");
        } catch (AssertionError e) {
            throw e;
        } catch (Throwable t) {
            if (!type.isInstance(t))
                throw new AssertionError(tag + ": expected " + type.getSimpleName() + " but got "
                        + t.getClass().getName() + ": " + t.getMessage(), t);
            if (exactMsg != null && !exactMsg.equals(t.getMessage()))
                throw new AssertionError(tag + ": " + type.getSimpleName() + " message <" + t.getMessage()
                        + "> want <" + exactMsg + ">");
            passed++;
        }
    }

    static void xeq(Object got, Object want, String tag) {
        if (got == null ? want != null : !got.equals(want))
            throw new AssertionError(tag + ": got <" + got + "> want <" + want + ">");
        passed++;
    }


    static void testDeepDTPE() {
        try {
            LocalDate.parse("2024-13-01");
            check(false, "unreachable");
        } catch (DateTimeParseException e) {
            check(e.getErrorIndex() == 0, "errorIndex 0 for value error");
            check(e.getMessage().equals("Text '2024-13-01' could not be parsed: Invalid value for MonthOfYear (valid values 1 - 12): 13"), "exact message");
            check(e.getCause() instanceof DateTimeException, "cause DTE");
        }
        try {
            LocalDate.parse("2024-02-29T10", DateTimeFormatter.ISO_LOCAL_DATE);
            check(false, "unreachable2");
        } catch (DateTimeParseException e) {
            check(e.getErrorIndex() == 10, "unparsed index 10");
            check(e.getParsedString().equals("2024-02-29T10"), "parsed string retained");
        }
        DateTimeParseException direct = new DateTimeParseException("m", "t", 7);
        check(direct.getErrorIndex() == 7 && direct.getParsedString().equals("t") && direct.getMessage().equals("m"), "ctor");
        DateTimeParseException withCause = new DateTimeParseException("m", "t", 3, new RuntimeException("r"));
        check(withCause.getCause() instanceof RuntimeException && withCause.getErrorIndex() == 3, "ctor cause");
        check(DateTimeParseException.class.getSuperclass() == DateTimeException.class, "extends DTE");
        try {
            LocalTime.parse("10:15:70");
            check(false, "unreachable3");
        } catch (DateTimeParseException e) {
            check(e.getErrorIndex() == 0, "time errorIndex");
        }
        try {
            LocalTime.parse("");
            check(false, "unreachable4");
        } catch (DateTimeParseException e) {
            check(e.getErrorIndex() == 0, "empty errorIndex 0");
        }
        try {
            Instant.parse("2024-02-29T10:15:30X");
            check(false, "unreachable5");
        } catch (DateTimeParseException e) {
            check(e.getErrorIndex() == 19, "instant suffix index 19");
        }
        check(new DateTimeParseException("x", "s", 0).toString().contains("DateTimeParseException"), "toString class");
    }

}
