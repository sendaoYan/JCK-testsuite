/*
 * @test
 * @summary java.net.UnknownHostException conformance: white-box structure + black-box behavior.
 * @run main UnknownHostExceptionTest
 */

import java.lang.reflect.*;
import java.net.*;

public class UnknownHostExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.UnknownHostException");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(java.io.IOException.class.isAssignableFrom(c), "extends IOException");
        check(c.getConstructor(String.class) != null, "ctor(String)");
        check(c.getConstructor() != null, "ctor()");
    }

    static void testBlackBox() throws Exception {
        UnknownHostException e = new UnknownHostException("no such host");
        check(e.getMessage().equals("no such host"), "message");
        check(e instanceof java.io.IOException, "instanceof IOException");
        try { throw e; } catch (java.io.IOException ioe) { passed++; }
        UnknownHostException e2 = new UnknownHostException();
        check(e2.getMessage() == null, "no-arg message null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
