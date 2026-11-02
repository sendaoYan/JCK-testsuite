/*
 * @test
 * @summary java.net.URL conformance: API type verification.
 * @run main URLTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class URLTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.net.URL u = new java.net.URL("http://example.com/a/b");
check(u.getProtocol().equals("http"), "protocol");
check(u.getHost().equals("example.com"), "host");
check(u.getPath().equals("/a/b"), "path");
check(u.getPort() == -1, "default port -1");
check(u.toExternalForm().equals("http://example.com/a/b"), "external form");
check(u.toURI().toString().equals("http://example.com/a/b"), "toURI");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
