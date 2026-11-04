/*
 * @test
 * @summary java.time.DateTimeException API conformance: hierarchy and the (String)
 *          constructor.
 * @run main DateTimeExceptionTest
 */

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.time.zone.ZoneRulesException;

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
        testDeepDTE();
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


    static void testDeepDTE() {
        DateTimeException e1 = new DateTimeException("boom");
        check(e1.getMessage().equals("boom"), "message");
        check(e1.getCause() == null, "cause null");
        DateTimeException e2 = new DateTimeException("bam", e1);
        check(e2.getCause() == e1, "cause chained");
        DateTimeException e4 = new DateTimeException(null);
        check(e4.getMessage() == null, "null message allowed");
        check(RuntimeException.class.isAssignableFrom(DateTimeException.class), "extends RuntimeException");
        check(DateTimeException.class.isAssignableFrom(java.time.temporal.UnsupportedTemporalTypeException.class),
            "UTE extends DTE");
        check(DateTimeException.class.isAssignableFrom(java.time.format.DateTimeParseException.class),
            "DTPE extends DTE");
        check(DateTimeException.class.isAssignableFrom(java.time.zone.ZoneRulesException.class),
            "ZRE extends DTE");
        // DTE thrown by core value classes carries a precise message
        try {
            LocalDate.of(2024, 2, 30);
            check(false, "unreachable");
        } catch (DateTimeException ex) {
            check(ex.getMessage().equals("Invalid date 'FEBRUARY 30'"), "DTE from of()");
        }
        try {
            LocalTime.of(24, 0);
            check(false, "unreachable2");
        } catch (DateTimeException ex) {
            check(ex.getMessage().startsWith("Invalid value for HourOfDay"), "DTE from LocalTime.of");
        }
        check(new DateTimeException("x").toString().equals("java.time.DateTimeException: x"), "toString");
    }

}
