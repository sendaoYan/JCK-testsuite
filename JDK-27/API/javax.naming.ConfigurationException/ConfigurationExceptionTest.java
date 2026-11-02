/*
 * @test
 * @summary javax.naming.ConfigurationException conformance: API type verification.
 * @run main ConfigurationExceptionTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class ConfigurationExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.naming.ConfigurationException e = new javax.naming.ConfigurationException("msg");
check("msg".equals(e.getMessage()), "message");
check(e instanceof javax.naming.NamingException, "extends javax.naming.NamingException");
check(e instanceof Exception, "is Exception");
javax.naming.ConfigurationException e2 = new javax.naming.ConfigurationException();
check(e2.getMessage() == null, "null message");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
