/*
 * @test
 * @summary java.net.NoRouteToHostException conformance: white-box structure + black-box behavior.
 * @run main NoRouteToHostExceptionTest
 */

import java.lang.reflect.*;
import java.net.*;

public class NoRouteToHostExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.NoRouteToHostException");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(SocketException.class.isAssignableFrom(c), "extends SocketException");
        check(c.getConstructor(String.class) != null, "ctor(String)");
        check(c.getConstructor() != null, "ctor()");
    }

    static void testBlackBox() throws Exception {
        NoRouteToHostException e = new NoRouteToHostException("no route");
        check(e.getMessage().equals("no route"), "message");
        check(e instanceof SocketException, "instanceof SocketException");
        try { throw e; } catch (SocketException se) { passed++; }
        NoRouteToHostException e2 = new NoRouteToHostException();
        check(e2.getMessage() == null, "no-arg message null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
