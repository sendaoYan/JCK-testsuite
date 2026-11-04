/*
 * @test
 * @summary java.time.temporal.UnsupportedTemporalTypeException conformance: white-box structure + black-box behavior.
 * @run main UnsupportedTemporalTypeExceptionTest
 */

import java.lang.reflect.Modifier;
import java.time.DateTimeException;
import java.time.LocalTime;
import java.time.temporal.ChronoField;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

public class UnsupportedTemporalTypeExceptionTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void expectThrows(Class<? extends Throwable> t, Runnable r, String m) {
        try { r.run(); } catch (Throwable e) {
            if (t.isInstance(e)) { passed++; return; }
            throw new AssertionError(m + ": expected " + t.getSimpleName() + " got " + e.getClass().getSimpleName(), e);
        }
        throw new AssertionError(m + ": expected " + t.getSimpleName() + " but nothing thrown");
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.time.temporal.UnsupportedTemporalTypeException");
        check(!c.isInterface(), "is a class");
        check(c.getSuperclass() == DateTimeException.class, "extends DateTimeException");
        Class<?> dt = Class.forName("java.time.DateTimeException");
        check(dt.getSuperclass() == RuntimeException.class, "DateTimeException extends RuntimeException");
        check(c.getConstructor(String.class) != null, "constructor (String)");
        check(c.getConstructor(String.class, Throwable.class) != null, "constructor (String,Throwable)");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // constructors + getMessage
        UnsupportedTemporalTypeException e1 = new UnsupportedTemporalTypeException("msg");
        check("msg".equals(e1.getMessage()), "getMessage");
        check(e1 instanceof DateTimeException, "is DateTimeException");
        check(e1 instanceof RuntimeException, "is RuntimeException");

        Throwable cause = new IllegalArgumentException("cause");
        UnsupportedTemporalTypeException e2 = new UnsupportedTemporalTypeException("outer", cause);
        check("outer".equals(e2.getMessage()), "ctor (String,Throwable) message");
        check(e2.getCause() == cause, "ctor (String,Throwable) cause");

        // produced naturally: query a date field on a time-only object
        expectThrows(UnsupportedTemporalTypeException.class,
                () -> LocalTime.of(10, 0).get(ChronoField.MONTH_OF_YEAR),
                "LocalTime.get(MONTH_OF_YEAR) throws UnsupportedTemporalTypeException");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepUnsupportedTemporalTypeException();
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


    static void testDeepUnsupportedTemporalTypeException() {
        UnsupportedTemporalTypeException e = new UnsupportedTemporalTypeException("no field");
        check(e.getMessage().equals("no field"), "message");
        check(e.getCause() == null, "cause null");
        UnsupportedTemporalTypeException e2 = new UnsupportedTemporalTypeException("bam", e);
        check(e2.getCause() == e, "cause chained");
        check(RuntimeException.class.isAssignableFrom(UnsupportedTemporalTypeException.class), "extends RuntimeException");
        check(DateTimeException.class.isAssignableFrom(UnsupportedTemporalTypeException.class), "extends DateTimeException");
        try {
            LocalDate.of(2024, 1, 1).getLong(ChronoField.HOUR_OF_DAY);
            check(false, "unreachable");
        } catch (UnsupportedTemporalTypeException ex) {
            check(ex.getMessage().equals("Unsupported field: HourOfDay"), "real UTE message");
        }
        try {
            LocalTime.NOON.plus(1, ChronoUnit.DAYS);
            check(false, "unreachable2");
        } catch (UnsupportedTemporalTypeException ex) {
            check(ex.getMessage().equals("Unsupported unit: Days"), "real UTE unit message");
        }
        check(new UnsupportedTemporalTypeException("x").toString().startsWith("java.time.temporal.UnsupportedTemporalTypeException: x"), "toString");
        check(!UnsupportedTemporalTypeException.class.equals(DateTimeParseException.class), "distinct from DTPE");
    }

}
