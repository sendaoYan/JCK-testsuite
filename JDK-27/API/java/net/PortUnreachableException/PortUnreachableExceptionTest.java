/*
 * @test
 * @summary java.net.PortUnreachableException conformance: white-box structure + black-box behavior.
 * @run main PortUnreachableExceptionTest
 */

import java.lang.reflect.*;
import java.net.*;

public class PortUnreachableExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.PortUnreachableException");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(SocketException.class.isAssignableFrom(c), "extends SocketException");
        check(c.getConstructor(String.class) != null, "ctor(String)");
        check(c.getConstructor() != null, "ctor()");
    }

    static void testBlackBox() throws Exception {
        PortUnreachableException e = new PortUnreachableException("icmp unreachable");
        check(e.getMessage().equals("icmp unreachable"), "message");
        check(e instanceof SocketException, "instanceof SocketException");
        try { throw e; } catch (SocketException se) { passed++; }
        PortUnreachableException e2 = new PortUnreachableException();
        check(e2.getMessage() == null, "no-arg message null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
