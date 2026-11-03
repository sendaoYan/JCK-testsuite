/*
 * @test
 * @summary java.net.HttpRetryException conformance: white-box structure + black-box behavior.
 * @run main HttpRetryExceptionTest
 */

import java.lang.reflect.*;
import java.net.*;

public class HttpRetryExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.HttpRetryException");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(java.io.IOException.class.isAssignableFrom(c), "extends IOException");
        check(c.getConstructor(String.class, int.class) != null, "ctor(String,int)");
        check(c.getConstructor(String.class, int.class, String.class) != null, "ctor(String,int,String)");
        check(c.getMethod("responseCode") != null, "responseCode");
        check(c.getMethod("getReason") != null, "getReason");
        check(c.getMethod("getLocation") != null, "getLocation");
    }

    static void testBlackBox() throws Exception {
        HttpRetryException e = new HttpRetryException("moved", 302);
        check(e.responseCode() == 302, "responseCode");
        check(e.getReason().equals("moved"), "reason");
        check(e.getLocation() == null, "location null default");
        check(e instanceof java.io.IOException, "instanceof IOException");

        HttpRetryException e2 = new HttpRetryException("redirect", 301, "http://example.com/");
        check(e2.responseCode() == 301, "responseCode with location");
        check(e2.getReason().equals("redirect"), "reason with location");
        check(e2.getLocation().equals("http://example.com/"), "location");

        try { throw e; } catch (java.io.IOException ioe) { passed++; }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
