/*
 * @test
 * @summary java.net.ConnectException conformance: API type verification.
 * @run main ConnectExceptionTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class ConnectExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.net.ConnectException e = new java.net.ConnectException("msg");
check("msg".equals(e.getMessage()), "message");
check(e instanceof java.net.SocketException, "extends java.net.SocketException");
check(e instanceof Exception, "is Exception");
java.net.ConnectException e2 = new java.net.ConnectException();
check(e2.getMessage() == null, "null message");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
