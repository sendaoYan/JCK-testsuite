/*
 * @test
 * @summary javax.naming.InsufficientResourcesException conformance: API type verification.
 * @run main InsufficientResourcesExceptionTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class InsufficientResourcesExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.naming.InsufficientResourcesException e = new javax.naming.InsufficientResourcesException("msg");
check("msg".equals(e.getMessage()), "message");
check(e instanceof javax.naming.NamingException, "extends javax.naming.NamingException");
check(e instanceof Exception, "is Exception");
javax.naming.InsufficientResourcesException e2 = new javax.naming.InsufficientResourcesException();
check(e2.getMessage() == null, "null message");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
