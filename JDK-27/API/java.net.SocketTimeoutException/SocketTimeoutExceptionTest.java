/*
 * @test
 * @summary java.net.SocketTimeoutException conformance: API type verification.
 * @run main SocketTimeoutExceptionTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class SocketTimeoutExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.net.SocketTimeoutException e = new java.net.SocketTimeoutException("msg");
check("msg".equals(e.getMessage()), "message");
check(e instanceof java.io.InterruptedIOException, "extends java.io.InterruptedIOException");
check(e instanceof Exception, "is Exception");
java.net.SocketTimeoutException e2 = new java.net.SocketTimeoutException();
check(e2.getMessage() == null, "null message");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
