/*
 * @test
 * @summary java.net.MalformedURLException conformance: API type verification.
 * @run main MalformedURLExceptionTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class MalformedURLExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.net.MalformedURLException e = new java.net.MalformedURLException("msg");
check("msg".equals(e.getMessage()), "message");
check(e instanceof java.io.IOException, "extends java.io.IOException");
check(e instanceof Exception, "is Exception");
java.net.MalformedURLException e2 = new java.net.MalformedURLException();
check(e2.getMessage() == null, "null message");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
