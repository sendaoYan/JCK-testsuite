/*
 * @test
 * @summary javax.naming.NoInitialContextException conformance: API type verification.
 * @run main NoInitialContextExceptionTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class NoInitialContextExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.naming.NoInitialContextException e = new javax.naming.NoInitialContextException("msg");
check("msg".equals(e.getMessage()), "message");
check(e instanceof javax.naming.NamingException, "extends javax.naming.NamingException");
check(e instanceof Exception, "is Exception");
javax.naming.NoInitialContextException e2 = new javax.naming.NoInitialContextException();
check(e2.getMessage() == null, "null message");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
