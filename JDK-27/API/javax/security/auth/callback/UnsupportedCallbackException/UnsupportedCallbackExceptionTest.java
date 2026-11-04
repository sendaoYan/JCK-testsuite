/*
 * @test
 * @summary javax.security.auth.callback.UnsupportedCallbackException conformance: white-box structure + black-box behavior.
 * @run main UnsupportedCallbackExceptionTest
 */

import java.lang.reflect.*;

public class UnsupportedCallbackExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.security.auth.callback.UnsupportedCallbackException");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface(), "is class");
        check(c.getSuperclass() == Exception.class, "extends Exception");
        check(hasMethod(c, "getCallback"), "method getCallback");
        check(c.getDeclaredConstructors().length == 2, "two constructors");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        javax.security.auth.callback.Callback cb = new javax.security.auth.callback.NameCallback("p");

        // Constructor 1: (callback).
        javax.security.auth.callback.UnsupportedCallbackException e =
            new javax.security.auth.callback.UnsupportedCallbackException(cb);
        check(e.getCallback() == cb, "getCallback returns same instance");
        check(e.getMessage() == null, "no-detail message is null");
        check(e instanceof Exception, "is Exception");

        // Constructor 2: (callback, message).
        javax.security.auth.callback.UnsupportedCallbackException e2 =
            new javax.security.auth.callback.UnsupportedCallbackException(cb, "m");
        check("m".equals(e2.getMessage()), "message ctor");
        check(e2.getCallback() == cb, "message ctor getCallback");

        // A callback can be null (the spec does not forbid it).
        javax.security.auth.callback.UnsupportedCallbackException e3 =
            new javax.security.auth.callback.UnsupportedCallbackException(null);
        check(e3.getCallback() == null, "null callback accepted");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
