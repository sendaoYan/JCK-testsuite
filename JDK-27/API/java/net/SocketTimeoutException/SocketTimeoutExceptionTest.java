/*
 * @test
 * @summary java.net.SocketTimeoutException conformance: white-box structure + black-box behavior.
 * @run main SocketTimeoutExceptionTest
 */

import java.lang.reflect.*;
import java.net.*;

public class SocketTimeoutExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.SocketTimeoutException");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(java.io.InterruptedIOException.class.isAssignableFrom(c), "extends InterruptedIOException");
        check(c.getConstructor(String.class) != null, "ctor(String)");
        check(c.getConstructor() != null, "ctor()");
    }

    static void testBlackBox() throws Exception {
        SocketTimeoutException e = new SocketTimeoutException("read timed out");
        check(e.getMessage().equals("read timed out"), "message");
        check(e instanceof java.io.InterruptedIOException, "instanceof InterruptedIOException");
        check(e instanceof java.io.IOException, "instanceof IOException");
        try { throw e; } catch (java.io.IOException ioe) { passed++; }
        SocketTimeoutException e2 = new SocketTimeoutException();
        check(e2.getMessage() == null, "no-arg message null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
