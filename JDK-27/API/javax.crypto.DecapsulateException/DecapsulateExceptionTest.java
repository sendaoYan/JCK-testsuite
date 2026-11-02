/*
 * @test
 * @summary javax.crypto.DecapsulateException conformance: decapsulate exception with cause ctors.
 * @run main DecapsulateExceptionTest
 */

import javax.crypto.DecapsulateException;

public class DecapsulateExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        DecapsulateException e = new DecapsulateException("msg");
        check("msg".equals(e.getMessage()), "message ctor");
        check(e instanceof java.security.GeneralSecurityException, "is GeneralSecurityException");
        Throwable c = new Throwable("cause");
        check(new DecapsulateException("m", c).getCause() == c, "cause ctor");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
