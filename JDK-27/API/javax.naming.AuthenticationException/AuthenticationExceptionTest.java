/*
 * @test
 * @summary javax.naming.AuthenticationException conformance: API type verification.
 * @run main AuthenticationExceptionTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class AuthenticationExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.naming.AuthenticationException e = new javax.naming.AuthenticationException("msg");
check("msg".equals(e.getMessage()), "message");
check(e instanceof javax.naming.NamingSecurityException, "extends javax.naming.NamingSecurityException");
check(e instanceof Exception, "is Exception");
javax.naming.AuthenticationException e2 = new javax.naming.AuthenticationException();
check(e2.getMessage() == null, "null message");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
