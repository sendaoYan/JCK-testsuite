/*
 * @test
 * @summary java.time.DateTimeException conformance: white-box hierarchy + black-box message/cause constructors.
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

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.time.DateTimeException");
        check(!c.isInterface() && !c.isEnum() && !c.isAnnotation(), "concrete class");
        check(c.getSuperclass().getName().equals("java.lang.RuntimeException"), "extends RuntimeException");
        check(c.getConstructor(String.class) != null, "ctor(String)");
        check(c.getConstructor(String.class, Throwable.class) != null, "ctor(String,Throwable)");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        DateTimeException e = new DateTimeException("msg");
        check(e instanceof RuntimeException, "is a RuntimeException");
        check("msg".equals(e.getMessage()), "(String) constructor");
        check(e.getCause() == null, "no cause by default");
        check(e.getStackTrace() != null, "getStackTrace non-null");

        Throwable cause = new IllegalArgumentException("cause");
        DateTimeException e2 = new DateTimeException("m2", cause);
        check("m2".equals(e2.getMessage()), "(String,Throwable) message");
        check(e2.getCause() == cause, "(String,Throwable) cause");
        check(e2 instanceof RuntimeException, "with-cause instance still RuntimeException");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
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
