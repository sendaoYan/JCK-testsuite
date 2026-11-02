/*
 * @test
 * @summary javax.naming.directory.AttributeInUseException conformance: API type verification.
 * @run main AttributeInUseExceptionTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class AttributeInUseExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.naming.directory.AttributeInUseException e = new javax.naming.directory.AttributeInUseException("msg");
check("msg".equals(e.getMessage()), "message");
check(e instanceof javax.naming.NamingException, "extends javax.naming.NamingException");
check(e instanceof Exception, "is Exception");
javax.naming.directory.AttributeInUseException e2 = new javax.naming.directory.AttributeInUseException();
check(e2.getMessage() == null, "null message");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
