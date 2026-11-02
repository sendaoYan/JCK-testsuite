/*
 * @test
 * @summary java.net.NoRouteToHostException conformance: API type verification.
 * @run main NoRouteToHostExceptionTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class NoRouteToHostExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.net.NoRouteToHostException e = new java.net.NoRouteToHostException("msg");
check("msg".equals(e.getMessage()), "message");
check(e instanceof java.net.SocketException, "extends java.net.SocketException");
check(e instanceof Exception, "is Exception");
java.net.NoRouteToHostException e2 = new java.net.NoRouteToHostException();
check(e2.getMessage() == null, "null message");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
