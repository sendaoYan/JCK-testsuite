/*
 * @test
 * @summary java.util.jar.JarException conformance: white-box structure + black-box behavior.
 * @run main JarExceptionTest
 */

import java.io.IOException;
import java.util.jar.JarException;
import java.util.zip.ZipException;

public class JarExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expectThrows(Class<? extends Throwable> type, Runnable action) {
        try {
            action.run();
            check(false, "expected " + type.getSimpleName());
        } catch (Throwable t) {
            check(type.isInstance(t),
                  "expected " + type.getSimpleName() + ", got " + t.getClass().getSimpleName());
        }
    }

    static boolean hasConstructor(Class<?> c, Class<?>... params) {
        try { c.getConstructor(params); return true; } catch (NoSuchMethodException e) { return false; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.jar.JarException");
        check(!c.isInterface() && java.lang.reflect.Modifier.isPublic(c.getModifiers()),
              "JarException is public concrete class");
        check(c.getSuperclass() == ZipException.class, "extends java.util.zip.ZipException");
        check(ZipException.class.getSuperclass() == IOException.class,
              "ZipException extends IOException");
        check(hasConstructor(c), "JarException()");
        check(hasConstructor(c, String.class), "JarException(String)");
        check(java.io.Serializable.class.isAssignableFrom(c), "is Serializable");
    }

    // ---- Part 2: black-box (behavioral) ----

    static void testConstructors() {
        JarException e = new JarException();
        check(e.getMessage() == null, "no-arg message null");
        check(e instanceof ZipException, "is a ZipException");
        check(e instanceof IOException, "is an IOException");

        JarException e2 = new JarException("bad jar");
        check("bad jar".equals(e2.getMessage()), "message ctor");
        check(e2 instanceof IOException, "message ctor is an IOException");

        JarException e3 = new JarException((String) null);
        check(e3.getMessage() == null, "null message stays null");
    }

    static void testThrowCatchRoundTrip() {
        final JarException thrown = new JarException("boom");
        try {
            throw thrown;
        } catch (JarException caught) {
            check(caught == thrown, "throw/catch preserves instance");
            check("boom".equals(caught.getMessage()), "message preserved");
        }

        // catchable as supertypes
        try {
            throw new JarException("x");
        } catch (ZipException caught) {
            check(caught instanceof JarException, "caught as ZipException");
        }
        try {
            throw new JarException("y");
        } catch (IOException caught) {
            check(caught instanceof JarException, "caught as IOException");
        }
    }

    static void testBlackBox() {
        testConstructors();
        testThrowCatchRoundTrip();
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
