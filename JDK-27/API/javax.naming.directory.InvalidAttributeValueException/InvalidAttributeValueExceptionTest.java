/*
 * @test
 * @summary javax.naming.directory.InvalidAttributeValueException conformance: API type verification.
 * @run main InvalidAttributeValueExceptionTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class InvalidAttributeValueExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.naming.directory.InvalidAttributeValueException e = new javax.naming.directory.InvalidAttributeValueException("msg");
check("msg".equals(e.getMessage()), "message");
check(e instanceof javax.naming.NamingException, "extends javax.naming.NamingException");
check(e instanceof Exception, "is Exception");
javax.naming.directory.InvalidAttributeValueException e2 = new javax.naming.directory.InvalidAttributeValueException();
check(e2.getMessage() == null, "null message");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
