/*
 * @test
 * @summary javax.naming.NamingException conformance: API type verification.
 * @run main NamingExceptionTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class NamingExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.naming.NamingException e = new javax.naming.NamingException("msg");
check("msg".equals(e.getMessage()), "message");
e.setResolvedName(new javax.naming.CompositeName("a"));
check(e.getResolvedName() != null, "resolved name");
check(e.getRemainingName() == null, "remaining name null");
check(e instanceof Exception, "Exception");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
