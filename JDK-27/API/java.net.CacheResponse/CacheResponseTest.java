/*
 * @test
 * @summary java.net.CacheResponse conformance: white-box structure + black-box behavior.
 * @run main CacheResponseTest
 */

import java.lang.reflect.*;
import java.net.*;
import java.io.*;
import java.util.*;

public class CacheResponseTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.CacheResponse");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getConstructor() != null, "no-arg ctor");
        check(c.getMethod("getHeaders") != null, "getHeaders");
        check(c.getMethod("getBody") != null, "getBody");
    }

    static void testBlackBox() throws Exception {
        final byte[] payload = "body".getBytes("UTF-8");
        final Map<String, List<String>> headers = new HashMap<>();
        headers.put("Content-Type", Collections.singletonList("text/plain"));
        CacheResponse resp = new CacheResponse() {
            public Map<String, List<String>> getHeaders() { return headers; }
            public InputStream getBody() { return new ByteArrayInputStream(payload); }
        };
        check(resp.getHeaders() == headers, "getHeaders");
        check(resp.getHeaders().get("Content-Type").get(0).equals("text/plain"), "header value");
        byte[] read = resp.getBody().readAllBytes();
        check(Arrays.equals(read, payload), "getBody bytes");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
