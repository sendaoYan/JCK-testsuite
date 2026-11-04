/*
 * @test
 * @summary java.time.zone.ZoneRulesException conformance: message and hierarchy.
 * @run main ZoneRulesExceptionTest
 */

import java.time.DateTimeException;
import java.time.zone.ZoneRulesException;
import java.time.ZoneId;

public class ZoneRulesExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testZoneRulesException() {
        ZoneRulesException e = new ZoneRulesException("msg");
        check("msg".equals(e.getMessage()), "getMessage");
        check(e instanceof DateTimeException, "is DateTimeException");
    }

    public static void main(String[] args) {
        testZoneRulesException();
        testDeepZRE();
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


    static void testDeepZRE() {
        ZoneRulesException e = new ZoneRulesException("no zone");
        check(e.getMessage().equals("no zone"), "message");
        check(e.getCause() == null, "cause null");
        ZoneRulesException chained = new ZoneRulesException("no zone", e);
        check(chained.getCause() == e, "chained cause");
        check(DateTimeException.class.isAssignableFrom(ZoneRulesException.class), "extends DTE");
        check(RuntimeException.class.isAssignableFrom(ZoneRulesException.class), "extends RuntimeException");
        try {
            ZoneId.of("Bogus/Zone");
            check(false, "unreachable");
        } catch (ZoneRulesException ex) {
            check(ex.getMessage().equals("Unknown time-zone ID: Bogus/Zone"), "message from ZoneId.of");
        }
        try {
            ZoneId.of("EST");
            check(false, "unreachable2");
        } catch (ZoneRulesException ex) {
            check(ex.getMessage().equals("Unknown time-zone ID: EST"), "3-letter unmapped");
        }
        check(new ZoneRulesException("x").toString().equals("java.time.zone.ZoneRulesException: x"), "toString");
        check(!e.equals("x"), "not equals foreign type");
    }

}
