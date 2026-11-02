/*
 * @test
 * @summary javax.net.ssl.SSLKeyException conformance: API type verification.
 * @run main SSLKeyExceptionTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class SSLKeyExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.net.ssl.SSLKeyException e = new javax.net.ssl.SSLKeyException("msg");
check("msg".equals(e.getMessage()), "message");
check(e instanceof javax.net.ssl.SSLException, "SSLException");
javax.net.ssl.SSLKeyException e2 = new javax.net.ssl.SSLKeyException("msg", e);
check(e2.getCause() == e, "cause");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
