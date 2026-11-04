/*
 * @test
 * @summary java.time.temporal.UnsupportedTemporalTypeException conformance: message and hierarchy.
 * @run main UnsupportedTemporalTypeExceptionTest
 */

import java.time.DateTimeException;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;

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
