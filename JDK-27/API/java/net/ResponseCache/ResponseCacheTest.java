/*
 * @test
 * @summary java.net.ResponseCache conformance: white-box structure + black-box behavior.
 * @run main ResponseCacheTest
 */

import java.lang.reflect.*;
import java.net.*;
import java.io.*;
import java.util.*;

public class ResponseCacheTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.ResponseCache");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getMethod("getDefault") != null, "getDefault");
        check(c.getMethod("setDefault", ResponseCache.class) != null, "setDefault");
        check(c.getMethod("get", URI.class, String.class, Map.class) != null, "get");
        check(c.getMethod("put", URI.class, URLConnection.class) != null, "put");
    }

    static void testBlackBox() throws Exception {
        ResponseCache prev = ResponseCache.getDefault();
        try {
            final boolean[] getCalled = { false };
            final boolean[] putCalled = { false };
            ResponseCache rc = new ResponseCache() {
                public CacheResponse get(URI uri, String rqstMethod, Map<String, List<String>> rqstHeaders) {
                    getCalled[0] = true;
                    return null;
                }
                public CacheRequest put(URI uri, URLConnection conn) {
                    putCalled[0] = true;
                    return null;
                }
            };
            ResponseCache.setDefault(rc);
            check(ResponseCache.getDefault() == rc, "setDefault/getDefault roundtrip");

            URI u = URI.create("http://example.com/");
            rc.get(u, "GET", new HashMap<>());
            check(getCalled[0], "get invoked");
            rc.put(u, null);
            check(putCalled[0], "put invoked");
        } finally {
            ResponseCache.setDefault(prev);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
