/*
 * @test
 * @summary javax.net.ssl.SSLProtocolException conformance: API type verification.
 * @run main SSLProtocolExceptionTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class SSLProtocolExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.net.ssl.SSLProtocolException e = new javax.net.ssl.SSLProtocolException("msg");
check("msg".equals(e.getMessage()), "message");
check(e instanceof javax.net.ssl.SSLException, "SSLException");
javax.net.ssl.SSLProtocolException e2 = new javax.net.ssl.SSLProtocolException("msg", e);
check(e2.getCause() == e, "cause");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
