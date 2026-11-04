/*
 * @test
 * @summary javax.security.auth.RefreshFailedException conformance: white-box structure + black-box behavior.
 * @run main RefreshFailedExceptionTest
 */

import java.lang.reflect.Modifier;
import javax.security.auth.RefreshFailedException;

public class RefreshFailedExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.security.auth.RefreshFailedException");
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
        RefreshFailedException e = new RefreshFailedException("msg");
        check("msg".equals(e.getMessage()), "message ctor");
        check(e instanceof Exception, "extends java.lang.Exception");

        check(new RefreshFailedException().getMessage() == null, "no-arg message null");

        RefreshFailedException caused = new RefreshFailedException("outer");
        IllegalStateException cause = new IllegalStateException("inner");
        check(caused.initCause(cause) == caused, "initCause returns this");
        check(caused.getCause() == cause, "getCause returns set cause");

        check(caused.getStackTrace().length >= 0, "getStackTrace callable");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
