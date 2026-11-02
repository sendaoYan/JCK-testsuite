/*
 * @test
 * @summary javax.naming.NameAlreadyBoundException conformance: API type verification.
 * @run main NameAlreadyBoundExceptionTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class NameAlreadyBoundExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.naming.NameAlreadyBoundException e = new javax.naming.NameAlreadyBoundException("msg");
check("msg".equals(e.getMessage()), "message");
check(e instanceof javax.naming.NamingException, "extends javax.naming.NamingException");
check(e instanceof Exception, "is Exception");
javax.naming.NameAlreadyBoundException e2 = new javax.naming.NameAlreadyBoundException();
check(e2.getMessage() == null, "null message");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
