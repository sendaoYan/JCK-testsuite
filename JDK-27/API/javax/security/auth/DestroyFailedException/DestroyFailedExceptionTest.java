/*
 * @test
 * @summary javax.security.auth.DestroyFailedException conformance: white-box structure + black-box behavior.
 * @run main DestroyFailedExceptionTest
 */

import java.lang.reflect.Modifier;
import javax.security.auth.DestroyFailedException;

public class DestroyFailedExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.security.auth.DestroyFailedException");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface(), "class not interface");
        check(c.getSuperclass() == Exception.class, "extends java.lang.Exception");
        check(!Modifier.isFinal(c.getModifiers()), "not final");
        check(hasPublicCtor(c), "no-arg ctor");
        check(hasPublicCtor(c, String.class), "String ctor");
    }

    static boolean hasPublicCtor(Class<?> c, Class<?>... params) {
        try { c.getConstructor(params); return true; } catch (NoSuchMethodException e) { return false; }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        DestroyFailedException e = new DestroyFailedException("msg");
        check("msg".equals(e.getMessage()), "message ctor");
        check(e instanceof Exception, "extends java.lang.Exception");

        check(new DestroyFailedException().getMessage() == null, "no-arg message null");

        // cause chain via initCause (no (String, Throwable) ctor on this class)
        DestroyFailedException caused = new DestroyFailedException("outer");
        IllegalStateException cause = new IllegalStateException("inner");
        check(caused.initCause(cause) == caused, "initCause returns this");
        check(caused.getCause() == cause, "getCause returns set cause");

        // serializable marker (Throwable hierarchy) is checked structurally via superclass only
        check(caused.getStackTrace().length >= 0, "getStackTrace callable");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
