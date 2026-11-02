/*
 * @test
 * @summary javax.naming.SizeLimitExceededException conformance: API type verification.
 * @run main SizeLimitExceededExceptionTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class SizeLimitExceededExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.naming.SizeLimitExceededException e = new javax.naming.SizeLimitExceededException("msg");
check("msg".equals(e.getMessage()), "message");
check(e instanceof javax.naming.LimitExceededException, "extends javax.naming.LimitExceededException");
check(e instanceof Exception, "is Exception");
javax.naming.SizeLimitExceededException e2 = new javax.naming.SizeLimitExceededException();
check(e2.getMessage() == null, "null message");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
