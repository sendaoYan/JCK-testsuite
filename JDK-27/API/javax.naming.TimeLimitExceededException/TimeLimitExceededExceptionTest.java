/*
 * @test
 * @summary javax.naming.TimeLimitExceededException conformance: API type verification.
 * @run main TimeLimitExceededExceptionTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class TimeLimitExceededExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.naming.TimeLimitExceededException e = new javax.naming.TimeLimitExceededException("msg");
check("msg".equals(e.getMessage()), "message");
check(e instanceof javax.naming.LimitExceededException, "extends javax.naming.LimitExceededException");
check(e instanceof Exception, "is Exception");
javax.naming.TimeLimitExceededException e2 = new javax.naming.TimeLimitExceededException();
check(e2.getMessage() == null, "null message");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
