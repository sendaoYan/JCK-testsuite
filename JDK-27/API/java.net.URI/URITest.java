/*
 * @test
 * @summary java.net.URI conformance: API type verification.
 * @run main URITest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class URITest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.net.URI u = java.net.URI.create("http://user@example.com:8080/a/b?x=1#frag");
check(u.getScheme().equals("http"), "scheme");
check(u.getHost().equals("example.com"), "host");
check(u.getPort() == 8080, "port");
check(u.getPath().equals("/a/b"), "path");
check(u.getQuery().equals("x=1"), "query");
check(u.getFragment().equals("frag"), "fragment");
check(u.isAbsolute(), "isAbsolute");
check(u.resolve("/c").toString().equals("http://user@example.com:8080/c"), "resolve");
check(java.net.URI.create("a/b").relativize(java.net.URI.create("a/b/c")).toString().equals("c"), "relativize");
check(new java.net.URI("http://example.com/").getHost().equals("example.com"), "parsed host");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
