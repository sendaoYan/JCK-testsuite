/*
 * @test
 * @summary javax.crypto.ShortBufferException conformance: short buffer exception hierarchy.
 * @run main ShortBufferExceptionTest
 */

import javax.crypto.ShortBufferException;

public class ShortBufferExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        ShortBufferException e = new ShortBufferException("msg");
        check("msg".equals(e.getMessage()), "message ctor");
        check(e instanceof java.security.GeneralSecurityException, "extends java.security.GeneralSecurityException");
        check(e instanceof java.security.GeneralSecurityException, "is java.security.GeneralSecurityException");
        check(new ShortBufferException().getMessage() == null, "no-arg message null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
