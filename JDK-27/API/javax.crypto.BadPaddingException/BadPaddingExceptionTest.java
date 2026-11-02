/*
 * @test
 * @summary javax.crypto.BadPaddingException conformance: bad padding exception hierarchy.
 * @run main BadPaddingExceptionTest
 */

import javax.crypto.BadPaddingException;

public class BadPaddingExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        BadPaddingException e = new BadPaddingException("msg");
        check("msg".equals(e.getMessage()), "message ctor");
        check(e instanceof java.security.GeneralSecurityException, "extends java.security.GeneralSecurityException");
        check(e instanceof java.security.GeneralSecurityException, "is java.security.GeneralSecurityException");
        check(new BadPaddingException().getMessage() == null, "no-arg message null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
