/*
 * @test
 * @summary java.net.UnknownServiceException conformance: white-box structure + black-box behavior.
 * @run main UnknownServiceExceptionTest
 */

import java.lang.reflect.*;
import java.net.*;

public class UnknownServiceExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.UnknownServiceException");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(java.io.IOException.class.isAssignableFrom(c), "extends IOException");
        check(c.getConstructor() != null, "ctor()");
        check(c.getConstructor(String.class) != null, "ctor(String)");
    }

    static void testBlackBox() throws Exception {
        UnknownServiceException e = new UnknownServiceException("no such service");
        check(e.getMessage().equals("no such service"), "message");
        check(e instanceof java.io.IOException, "instanceof IOException");
        try { throw e; } catch (java.io.IOException ioe) { passed++; }
        UnknownServiceException e2 = new UnknownServiceException();
        check(e2.getMessage() == null, "no-arg message null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
