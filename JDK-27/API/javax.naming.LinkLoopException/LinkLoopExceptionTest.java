/*
 * @test
 * @summary javax.naming.LinkLoopException conformance: API type verification.
 * @run main LinkLoopExceptionTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class LinkLoopExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.naming.LinkLoopException e = new javax.naming.LinkLoopException("msg");
check("msg".equals(e.getMessage()), "message");
check(e instanceof javax.naming.LinkException, "extends javax.naming.LinkException");
check(e instanceof Exception, "is Exception");
javax.naming.LinkLoopException e2 = new javax.naming.LinkLoopException();
check(e2.getMessage() == null, "null message");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
