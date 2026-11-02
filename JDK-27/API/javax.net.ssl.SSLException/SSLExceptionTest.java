/*
 * @test
 * @summary javax.net.ssl.SSLException conformance: API type verification.
 * @run main SSLExceptionTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class SSLExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.net.ssl.SSLException e = new javax.net.ssl.SSLException("msg");
check("msg".equals(e.getMessage()), "message");
check(e instanceof java.io.IOException, "IOException");
javax.net.ssl.SSLException e2 = new javax.net.ssl.SSLException("msg", e);
check(e2.getCause() == e, "cause");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
