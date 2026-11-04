/*
 * @test
 * @summary java.net.BindException conformance: white-box structure + black-box behavior.
 * @run main BindExceptionTest
 */

import java.lang.reflect.*;
import java.net.*;

public class BindExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.BindException");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(SocketException.class.isAssignableFrom(c), "extends SocketException");
        check(java.io.IOException.class.isAssignableFrom(c), "is IOException");
        check(c.getConstructor(String.class) != null, "ctor(String)");
        check(c.getConstructor() != null, "ctor()");
    }

    static void testBlackBox() throws Exception {
        BindException e = new BindException("port in use");
        check(e.getMessage().equals("port in use"), "message");
        check(e instanceof SocketException, "instanceof SocketException");
        check(e instanceof java.io.IOException, "instanceof IOException");
        // catchable via supertype
        try { throw e; } catch (SocketException se) { passed++; }
        BindException e2 = new BindException();
        check(e2.getMessage() == null, "no-arg message null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
