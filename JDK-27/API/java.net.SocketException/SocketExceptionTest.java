/*
 * @test
 * @summary java.net.SocketException conformance: API type verification.
 * @run main SocketExceptionTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class SocketExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.net.SocketException e = new java.net.SocketException("msg");
check("msg".equals(e.getMessage()), "message");
check(e instanceof java.io.IOException, "extends java.io.IOException");
check(e instanceof Exception, "is Exception");
java.net.SocketException e2 = new java.net.SocketException();
check(e2.getMessage() == null, "null message");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
