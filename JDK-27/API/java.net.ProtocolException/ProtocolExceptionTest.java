/*
 * @test
 * @summary java.net.ProtocolException conformance: API type verification.
 * @run main ProtocolExceptionTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class ProtocolExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.net.ProtocolException e = new java.net.ProtocolException("msg");
check("msg".equals(e.getMessage()), "message");
check(e instanceof java.io.IOException, "extends java.io.IOException");
check(e instanceof Exception, "is Exception");
java.net.ProtocolException e2 = new java.net.ProtocolException();
check(e2.getMessage() == null, "null message");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
