/*
 * @test
 * @summary javax.naming.NameNotFoundException conformance: API type verification.
 * @run main NameNotFoundExceptionTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class NameNotFoundExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.naming.NameNotFoundException e = new javax.naming.NameNotFoundException("msg");
check("msg".equals(e.getMessage()), "message");
check(e instanceof javax.naming.NamingException, "extends javax.naming.NamingException");
check(e instanceof Exception, "is Exception");
javax.naming.NameNotFoundException e2 = new javax.naming.NameNotFoundException();
check(e2.getMessage() == null, "null message");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
