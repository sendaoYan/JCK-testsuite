/*
 * @test
 * @summary javax.naming.AuthenticationNotSupportedException conformance: API type verification.
 * @run main AuthenticationNotSupportedExceptionTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class AuthenticationNotSupportedExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.naming.AuthenticationNotSupportedException e = new javax.naming.AuthenticationNotSupportedException("msg");
check("msg".equals(e.getMessage()), "message");
check(e instanceof javax.naming.NamingSecurityException, "extends javax.naming.NamingSecurityException");
check(e instanceof Exception, "is Exception");
javax.naming.AuthenticationNotSupportedException e2 = new javax.naming.AuthenticationNotSupportedException();
check(e2.getMessage() == null, "null message");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
