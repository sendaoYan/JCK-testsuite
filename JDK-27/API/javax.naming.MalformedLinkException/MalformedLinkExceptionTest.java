/*
 * @test
 * @summary javax.naming.MalformedLinkException conformance: API type verification.
 * @run main MalformedLinkExceptionTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class MalformedLinkExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.naming.MalformedLinkException e = new javax.naming.MalformedLinkException("msg");
check("msg".equals(e.getMessage()), "message");
check(e instanceof javax.naming.LinkException, "extends javax.naming.LinkException");
check(e instanceof Exception, "is Exception");
javax.naming.MalformedLinkException e2 = new javax.naming.MalformedLinkException();
check(e2.getMessage() == null, "null message");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
