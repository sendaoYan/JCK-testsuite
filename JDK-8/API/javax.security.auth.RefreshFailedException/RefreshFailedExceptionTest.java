/*
 * @test
 * @summary javax.security.auth.RefreshFailedException conformance: constructors, message, cause and superclass.
 * @run main RefreshFailedExceptionTest
 */

public class RefreshFailedExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.security.auth.RefreshFailedException e = new javax.security.auth.RefreshFailedException("boom");
        check("boom".equals(e.getMessage()), "String ctor sets message");
        check(e instanceof java.lang.Exception, "extends java.lang.Exception");
        javax.security.auth.RefreshFailedException e4 = new javax.security.auth.RefreshFailedException();
        check(e4.getMessage() == null, "no-arg ctor message null");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
