/*
 * @test
 * @summary java.net.ConnectException conformance: white-box structure + black-box behavior.
 * @run main ConnectExceptionTest
 */

import java.lang.reflect.*;
import java.net.*;

public class ConnectExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.ConnectException");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(SocketException.class.isAssignableFrom(c), "extends SocketException");
        check(c.getConstructor(String.class) != null, "ctor(String)");
        check(c.getConstructor() != null, "ctor()");
    }

    static void testBlackBox() throws Exception {
        ConnectException e = new ConnectException("connection refused");
        check(e.getMessage().equals("connection refused"), "message");
        check(e instanceof SocketException, "instanceof SocketException");
        check(e instanceof java.io.IOException, "instanceof IOException");
        try { throw e; } catch (SocketException se) { passed++; }
        ConnectException e2 = new ConnectException();
        check(e2.getMessage() == null, "no-arg message null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
