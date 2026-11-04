/*
 * @test
 * @summary java.net.CacheRequest conformance: white-box structure + black-box behavior.
 * @run main CacheRequestTest
 */

import java.lang.reflect.*;
import java.net.*;
import java.io.*;

public class CacheRequestTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.CacheRequest");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getConstructor() != null, "no-arg ctor");
        check(c.getMethod("getBody") != null, "getBody");
        check(c.getMethod("abort") != null, "abort");
    }

    static void testBlackBox() throws Exception {
        final ByteArrayOutputStream body = new ByteArrayOutputStream();
        final boolean[] aborted = { false };
        CacheRequest req = new CacheRequest() {
            public OutputStream getBody() { return body; }
            public void abort() { aborted[0] = true; }
        };
        OutputStream os = req.getBody();
        os.write("cached".getBytes("UTF-8"));
        check(new String(body.toByteArray(), "UTF-8").equals("cached"), "body written");
        check(!aborted[0], "not aborted yet");
        req.abort();
        check(aborted[0], "aborted");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
