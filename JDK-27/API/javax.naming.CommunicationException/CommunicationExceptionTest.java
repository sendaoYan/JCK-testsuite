/*
 * @test
 * @summary javax.naming.CommunicationException conformance: API type verification.
 * @run main CommunicationExceptionTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class CommunicationExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.naming.CommunicationException e = new javax.naming.CommunicationException("msg");
check("msg".equals(e.getMessage()), "message");
check(e instanceof javax.naming.NamingException, "extends javax.naming.NamingException");
check(e instanceof Exception, "is Exception");
javax.naming.CommunicationException e2 = new javax.naming.CommunicationException();
check(e2.getMessage() == null, "null message");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
