/*
 * @test
 * @summary javax.security.auth.DestroyFailedException conformance: constructors, message, cause and superclass.
 * @run main DestroyFailedExceptionTest
 */

public class DestroyFailedExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.security.auth.DestroyFailedException e = new javax.security.auth.DestroyFailedException("boom");
        check("boom".equals(e.getMessage()), "String ctor sets message");
        check(e instanceof java.lang.Exception, "extends java.lang.Exception");
        javax.security.auth.DestroyFailedException e4 = new javax.security.auth.DestroyFailedException();
        check(e4.getMessage() == null, "no-arg ctor message null");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
