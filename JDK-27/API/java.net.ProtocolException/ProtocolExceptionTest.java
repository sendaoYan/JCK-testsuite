/*
 * @test
 * @summary java.net.ProtocolException conformance: white-box structure + black-box behavior.
 * @run main ProtocolExceptionTest
 */

import java.lang.reflect.*;
import java.net.*;

public class ProtocolExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.ProtocolException");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(java.io.IOException.class.isAssignableFrom(c), "extends IOException");
        check(c.getSuperclass() == java.io.IOException.class, "direct superclass IOException");
        check(c.getConstructor(String.class) != null, "ctor(String)");
        check(c.getConstructor() != null, "ctor()");
    }

    static void testBlackBox() throws Exception {
        ProtocolException e = new ProtocolException("bad method");
        check(e.getMessage().equals("bad method"), "message");
        check(e instanceof java.io.IOException, "instanceof IOException");
        try { throw e; } catch (java.io.IOException ioe) { passed++; }
        ProtocolException e2 = new ProtocolException();
        check(e2.getMessage() == null, "no-arg message null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
