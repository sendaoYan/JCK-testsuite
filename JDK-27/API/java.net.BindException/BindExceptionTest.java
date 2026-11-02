/*
 * @test
 * @summary java.net.BindException conformance: API type verification.
 * @run main BindExceptionTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class BindExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.net.BindException e = new java.net.BindException("msg");
check("msg".equals(e.getMessage()), "message");
check(e instanceof java.net.SocketException, "extends java.net.SocketException");
check(e instanceof Exception, "is Exception");
java.net.BindException e2 = new java.net.BindException();
check(e2.getMessage() == null, "null message");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
