/*
 * @test
 * @summary javax.crypto.NoSuchPaddingException conformance: no such padding exception hierarchy.
 * @run main NoSuchPaddingExceptionTest
 */

import javax.crypto.NoSuchPaddingException;

public class NoSuchPaddingExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        NoSuchPaddingException e = new NoSuchPaddingException("msg");
        check("msg".equals(e.getMessage()), "message ctor");
        check(e instanceof java.security.GeneralSecurityException, "extends java.security.GeneralSecurityException");
        check(e instanceof java.security.GeneralSecurityException, "is java.security.GeneralSecurityException");
        check(new NoSuchPaddingException().getMessage() == null, "no-arg message null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
