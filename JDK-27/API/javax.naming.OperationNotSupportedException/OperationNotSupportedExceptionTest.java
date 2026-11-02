/*
 * @test
 * @summary javax.naming.OperationNotSupportedException conformance: API type verification.
 * @run main OperationNotSupportedExceptionTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class OperationNotSupportedExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.naming.OperationNotSupportedException e = new javax.naming.OperationNotSupportedException("msg");
check("msg".equals(e.getMessage()), "message");
check(e instanceof javax.naming.NamingException, "extends javax.naming.NamingException");
check(e instanceof Exception, "is Exception");
javax.naming.OperationNotSupportedException e2 = new javax.naming.OperationNotSupportedException();
check(e2.getMessage() == null, "null message");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
