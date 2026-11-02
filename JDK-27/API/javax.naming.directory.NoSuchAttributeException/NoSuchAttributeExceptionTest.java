/*
 * @test
 * @summary javax.naming.directory.NoSuchAttributeException conformance: API type verification.
 * @run main NoSuchAttributeExceptionTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class NoSuchAttributeExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.naming.directory.NoSuchAttributeException e = new javax.naming.directory.NoSuchAttributeException("msg");
check("msg".equals(e.getMessage()), "message");
check(e instanceof javax.naming.NamingException, "extends javax.naming.NamingException");
check(e instanceof Exception, "is Exception");
javax.naming.directory.NoSuchAttributeException e2 = new javax.naming.directory.NoSuchAttributeException();
check(e2.getMessage() == null, "null message");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
