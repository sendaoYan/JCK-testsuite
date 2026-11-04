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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
