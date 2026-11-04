/*
 * @test
 * @summary javax.smartcardio.CardNotPresentException conformance: white-box structure + black-box behavior.
 * @run main CardNotPresentExceptionTest
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class CardNotPresentExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.smartcardio.CardNotPresentException");
        check(!Modifier.isAbstract(c.getModifiers()), "CardNotPresentException concrete");
        check(javax.smartcardio.CardException.class.isAssignableFrom(c), "CardNotPresentException extends CardException");
        check(c.getSuperclass() == javax.smartcardio.CardException.class, "CardNotPresentException direct superclass");
        int ctorCount = 0;
        for (Constructor<?> ct : c.getConstructors()) ctorCount++;
        check(ctorCount == 3, "CardNotPresentException has 3 public ctors (got " + ctorCount + ")");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        javax.smartcardio.CardNotPresentException e = new javax.smartcardio.CardNotPresentException("no card");
        check("no card".equals(e.getMessage()), "CardNotPresentException getMessage");
        check(e.getCause() == null, "CardNotPresentException no cause for String ctor");
        check(e instanceof javax.smartcardio.CardException, "CardNotPresentException is-a CardException");

        RuntimeException cause = new RuntimeException("c");
        javax.smartcardio.CardNotPresentException e2 = new javax.smartcardio.CardNotPresentException(cause);
        check(cause == e2.getCause(), "CardNotPresentException cause identity");
        check(cause.toString().equals(e2.getMessage()), "CardNotPresentException message == cause.toString()");

        javax.smartcardio.CardNotPresentException e3 = new javax.smartcardio.CardNotPresentException("m", cause);
        check("m".equals(e3.getMessage()), "CardNotPresentException message String+Throwable");
        check(cause == e3.getCause(), "CardNotPresentException cause String+Throwable");

        // catchable via the parent CardException type (polymorphic)
        boolean caught = false;
        try { throw new javax.smartcardio.CardNotPresentException("x"); }
        catch (javax.smartcardio.CardException ex) { caught = true; }
        check(caught, "CardNotPresentException catchable as CardException");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
