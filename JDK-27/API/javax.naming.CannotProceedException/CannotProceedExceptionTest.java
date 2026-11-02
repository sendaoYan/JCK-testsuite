/*
 * @test
 * @summary javax.naming.CannotProceedException conformance: API type verification.
 * @run main CannotProceedExceptionTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class CannotProceedExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.naming.CannotProceedException e = new javax.naming.CannotProceedException("msg");
check("msg".equals(e.getMessage()), "message");
java.util.Hashtable<String,Object> env = new java.util.Hashtable<>();
env.put("k", "v");
e.setEnvironment(env);
check(e.getEnvironment() == env, "environment");
check(e instanceof javax.naming.NamingException, "NamingException");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
