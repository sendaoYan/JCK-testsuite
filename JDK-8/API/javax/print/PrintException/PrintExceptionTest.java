/*
 * @test
 * @summary javax.print.PrintException conformance: exception class.
 * @run main PrintExceptionTest
 */

import java.lang.reflect.*;


public class PrintExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] a) throws Exception {
        javax.print.PrintException e1 = new javax.print.PrintException();
        check(e1.getMessage() == null, "default message");
        check(e1 instanceof Exception, "extends Exception");
        javax.print.PrintException e2 = new javax.print.PrintException("msg");
        check("msg".equals(e2.getMessage()), "message ctor");
        Exception cause = new Exception("cause");
        javax.print.PrintException e3 = new javax.print.PrintException(cause);
        check(e3.getCause() == cause, "cause ctor");
        javax.print.PrintException e4 = new javax.print.PrintException("m", cause);
        check("m".equals(e4.getMessage()) && e4.getCause() == cause, "message+cause ctor");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
