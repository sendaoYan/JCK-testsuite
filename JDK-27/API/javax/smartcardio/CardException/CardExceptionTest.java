/*
 * @test
 * @summary javax.smartcardio.CardException conformance: white-box structure + black-box behavior.
 * @run main CardExceptionTest
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class CardExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.smartcardio.CardException");
        check(!Modifier.isAbstract(c.getModifiers()), "CardException concrete");
        check(Exception.class.isAssignableFrom(c), "CardException extends Exception");
        check(c.getSuperclass() == Exception.class, "CardException direct superclass Exception");
        // three constructors: String, Throwable, String+Throwable
        int ctorCount = 0;
        for (Constructor<?> ct : c.getConstructors()) ctorCount++;
        check(ctorCount == 3, "CardException has 3 public ctors (got " + ctorCount + ")");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // String ctor
        javax.smartcardio.CardException e = new javax.smartcardio.CardException("boom");
        check("boom".equals(e.getMessage()), "CardException getMessage");
        check(e.getCause() == null, "CardException no cause for String ctor");

        // Throwable ctor: message = cause.toString()
        RuntimeException cause = new RuntimeException("c");
        javax.smartcardio.CardException e2 = new javax.smartcardio.CardException(cause);
        check(cause == e2.getCause(), "CardException getCause identity");
        check(cause.toString().equals(e2.getMessage()), "CardException message == cause.toString()");

        // String + Throwable ctor
        javax.smartcardio.CardException e3 = new javax.smartcardio.CardException("m", cause);
        check("m".equals(e3.getMessage()), "CardException message String+Throwable");
        check(cause == e3.getCause(), "CardException cause String+Throwable");

        // detail chain: initCause / printStackTrace-style stack trace preserved
        javax.smartcardio.CardException e4 = new javax.smartcardio.CardException("outer");
        e4.initCause(new IllegalStateException("inner"));
        check(e4.getCause() instanceof IllegalStateException, "CardException initCause chain");

        // usable in try/catch as a checked Exception
        boolean caught = false;
        try { throw new javax.smartcardio.CardException("x"); }
        catch (javax.smartcardio.CardException ex) { caught = true; }
        check(caught, "CardException catchable as checked exception");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
