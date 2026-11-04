/*
 * @test
 * @summary java.net.SocketException conformance: white-box structure + black-box behavior.
 * @run main SocketExceptionTest
 */

import java.lang.reflect.*;
import java.net.*;

public class SocketExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.SocketException");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(java.io.IOException.class.isAssignableFrom(c), "extends IOException");
        check(c.getConstructor() != null, "ctor()");
        check(c.getConstructor(String.class) != null, "ctor(String)");
        check(c.getConstructor(String.class, Throwable.class) != null, "ctor(String,Throwable)");
        check(c.getConstructor(Throwable.class) != null, "ctor(Throwable)");
    }

    static void testBlackBox() throws Exception {
        SocketException e = new SocketException("boom");
        check(e.getMessage().equals("boom"), "message");
        check(e instanceof java.io.IOException, "instanceof IOException");

        Throwable cause = new RuntimeException("cause");
        SocketException chained = new SocketException("outer", cause);
        check(chained.getMessage().equals("outer"), "chained message");
        check(chained.getCause() == cause, "chained cause");

        SocketException causeOnly = new SocketException(cause);
        check(causeOnly.getCause() == cause, "cause-only");
        check(causeOnly.getMessage() == null || causeOnly.getMessage().equals(cause.toString()), "cause-only message");

        SocketException empty = new SocketException();
        check(empty.getMessage() == null, "no-arg message null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
