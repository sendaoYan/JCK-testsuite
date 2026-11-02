/*
 * @test
 * @summary javax.security.auth.RefreshFailedException conformance: refresh failed exception hierarchy.
 * @run main RefreshFailedExceptionTest
 */

import javax.security.auth.RefreshFailedException;

public class RefreshFailedExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        RefreshFailedException e = new RefreshFailedException("msg");
        check("msg".equals(e.getMessage()), "message ctor");
        check(e instanceof java.lang.Exception, "extends java.lang.Exception");
        check(e instanceof java.lang.Exception, "is java.lang.Exception");
        check(new RefreshFailedException().getMessage() == null, "no-arg message null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
