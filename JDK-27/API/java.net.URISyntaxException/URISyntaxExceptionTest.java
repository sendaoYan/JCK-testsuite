/*
 * @test
 * @summary java.net.URISyntaxException conformance: API type verification.
 * @run main URISyntaxExceptionTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class URISyntaxExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.net.URISyntaxException e = new java.net.URISyntaxException("input", "reason", 3);
check(e.getInput().equals("input"), "input");
check(e.getReason().equals("reason"), "reason");
check(e.getIndex() == 3, "index");
check(e.getMessage() != null, "message");
check(e instanceof Exception, "Exception");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
