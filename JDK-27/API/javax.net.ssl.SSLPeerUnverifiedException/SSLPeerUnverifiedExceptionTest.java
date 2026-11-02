/*
 * @test
 * @summary javax.net.ssl.SSLPeerUnverifiedException conformance: API type verification.
 * @run main SSLPeerUnverifiedExceptionTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class SSLPeerUnverifiedExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.net.ssl.SSLPeerUnverifiedException e = new javax.net.ssl.SSLPeerUnverifiedException("msg");
check("msg".equals(e.getMessage()), "message");
check(e instanceof javax.net.ssl.SSLException, "SSLException");
javax.net.ssl.SSLPeerUnverifiedException e2 = new javax.net.ssl.SSLPeerUnverifiedException("msg", e);
check(e2.getCause() == e, "cause");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
