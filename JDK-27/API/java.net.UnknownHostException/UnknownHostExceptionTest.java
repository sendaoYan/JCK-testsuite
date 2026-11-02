/*
 * @test
 * @summary java.net.UnknownHostException conformance: API type verification.
 * @run main UnknownHostExceptionTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class UnknownHostExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.net.UnknownHostException e = new java.net.UnknownHostException("msg");
check("msg".equals(e.getMessage()), "message");
check(e instanceof java.io.IOException, "extends java.io.IOException");
check(e instanceof Exception, "is Exception");
java.net.UnknownHostException e2 = new java.net.UnknownHostException();
check(e2.getMessage() == null, "null message");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
