/*
 * @test
 * @summary java.time.zone.ZoneRulesException conformance: white-box structure + black-box behavior.
 * @run main ZoneRulesExceptionTest
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.time.DateTimeException;
import java.time.zone.ZoneRulesException;
import java.time.zone.ZoneRulesProvider;
import java.time.ZoneId;

public class ZoneRulesExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expectThrows(Class<? extends Throwable> t, Runnable r, String msg) {
        try {
            r.run();
        } catch (Throwable e) {
            if (t.isInstance(e)) { passed++; return; }
            throw new AssertionError(msg + ": expected " + t.getSimpleName()
                    + " got " + e.getClass().getSimpleName(), e);
        }
        throw new AssertionError(msg + ": expected " + t.getSimpleName() + " but nothing thrown");
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.time.zone.ZoneRulesException");
        check(!c.isInterface(), "not interface");
        check(!Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(!c.isEnum(), "not enum");
        check(c.getSuperclass() == DateTimeException.class, "extends DateTimeException");
        check(RuntimeException.class.isAssignableFrom(c), "is a RuntimeException");

        boolean hasString = false;
        boolean hasStringThrowable = false;
        for (Constructor<?> k : c.getConstructors()) {
            Class<?>[] p = k.getParameterTypes();
            if (p.length == 1 && p[0] == String.class) hasString = true;
            if (p.length == 2 && p[0] == String.class && p[1] == Throwable.class) hasStringThrowable = true;
        }
        check(hasString, "constructor (String) present");
        check(hasStringThrowable, "constructor (String, Throwable) present");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        ZoneRulesException e = new ZoneRulesException("msg");
        check("msg".equals(e.getMessage()), "getMessage");
        check(e instanceof DateTimeException, "is DateTimeException");
        check(e instanceof RuntimeException, "is RuntimeException");

        Throwable cause = new IllegalArgumentException("root");
        ZoneRulesException e2 = new ZoneRulesException("msg", cause);
        check("msg".equals(e2.getMessage()), "getMessage with cause");
        check(e2.getCause() == cause, "getCause");

        // A bogus zone id surfaces this exception from ZoneRulesProvider.
        expectThrows(ZoneRulesException.class,
                     () -> ZoneRulesProvider.getRules("Not/AZone", false),
                     "bogus zone id");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
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
