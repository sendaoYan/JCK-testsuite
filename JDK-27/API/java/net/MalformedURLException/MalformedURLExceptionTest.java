/*
 * @test
 * @summary java.net.MalformedURLException conformance: white-box structure + black-box behavior.
 * @run main MalformedURLExceptionTest
 */

import java.lang.reflect.*;
import java.net.*;

public class MalformedURLExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.MalformedURLException");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(java.io.IOException.class.isAssignableFrom(c), "extends IOException");
        check(c.getConstructor() != null, "ctor()");
        check(c.getConstructor(String.class) != null, "ctor(String)");
    }

    static void testBlackBox() throws Exception {
        MalformedURLException e = new MalformedURLException("bad url");
        check(e.getMessage().equals("bad url"), "message");
        check(e instanceof java.io.IOException, "instanceof IOException");
        try { throw e; } catch (java.io.IOException ioe) { passed++; }
        MalformedURLException e2 = new MalformedURLException();
        check(e2.getMessage() == null, "no-arg message null");
        // actually thrown by URL parsing
        try { new URL("nosuchproto://x/"); } catch (MalformedURLException ex) { passed++; }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
