/*
 * @test
 * @summary java.net.HttpRetryException conformance: API type verification.
 * @run main HttpRetryExceptionTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class HttpRetryExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.net.HttpRetryException e = new java.net.HttpRetryException("detail", 404);
check(e.responseCode() == 404, "responseCode");
check("detail".equals(e.getReason()), "reason");
check(e.getLocation() == null, "location null");
check(e instanceof java.io.IOException, "IOException");
java.net.HttpRetryException e2 = new java.net.HttpRetryException("d", 500, "http://example.com/");
check(e2.getLocation().equals("http://example.com/"), "location");
check(e2.responseCode() == 500, "code");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
