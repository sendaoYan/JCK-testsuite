/*
 * @test
 * @summary javax.security.auth.DestroyFailedException conformance: destroy failed exception hierarchy.
 * @run main DestroyFailedExceptionTest
 */

import javax.security.auth.DestroyFailedException;

public class DestroyFailedExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        DestroyFailedException e = new DestroyFailedException("msg");
        check("msg".equals(e.getMessage()), "message ctor");
        check(e instanceof java.lang.Exception, "extends java.lang.Exception");
        check(e instanceof java.lang.Exception, "is java.lang.Exception");
        check(new DestroyFailedException().getMessage() == null, "no-arg message null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
