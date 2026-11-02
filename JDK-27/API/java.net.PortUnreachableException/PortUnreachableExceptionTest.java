/*
 * @test
 * @summary java.net.PortUnreachableException conformance: API type verification.
 * @run main PortUnreachableExceptionTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class PortUnreachableExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.net.PortUnreachableException e = new java.net.PortUnreachableException("msg");
check("msg".equals(e.getMessage()), "message");
check(e instanceof java.net.SocketException, "extends java.net.SocketException");
check(e instanceof Exception, "is Exception");
java.net.PortUnreachableException e2 = new java.net.PortUnreachableException();
check(e2.getMessage() == null, "null message");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
