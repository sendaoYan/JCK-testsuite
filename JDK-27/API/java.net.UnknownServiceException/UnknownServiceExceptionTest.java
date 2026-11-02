/*
 * @test
 * @summary java.net.UnknownServiceException conformance: API type verification.
 * @run main UnknownServiceExceptionTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class UnknownServiceExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.net.UnknownServiceException e = new java.net.UnknownServiceException("msg");
check("msg".equals(e.getMessage()), "message");
check(e instanceof java.io.IOException, "extends java.io.IOException");
check(e instanceof Exception, "is Exception");
java.net.UnknownServiceException e2 = new java.net.UnknownServiceException();
check(e2.getMessage() == null, "null message");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
