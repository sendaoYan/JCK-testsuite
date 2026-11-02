/*
 * @test
 * @summary javax.naming.NoPermissionException conformance: API type verification.
 * @run main NoPermissionExceptionTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class NoPermissionExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.naming.NoPermissionException e = new javax.naming.NoPermissionException("msg");
check("msg".equals(e.getMessage()), "message");
check(e instanceof javax.naming.NamingSecurityException, "extends javax.naming.NamingSecurityException");
check(e instanceof Exception, "is Exception");
javax.naming.NoPermissionException e2 = new javax.naming.NoPermissionException();
check(e2.getMessage() == null, "null message");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
