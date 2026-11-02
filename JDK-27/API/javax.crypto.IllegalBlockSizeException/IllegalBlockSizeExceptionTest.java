/*
 * @test
 * @summary javax.crypto.IllegalBlockSizeException conformance: illegal block size exception hierarchy.
 * @run main IllegalBlockSizeExceptionTest
 */

import javax.crypto.IllegalBlockSizeException;

public class IllegalBlockSizeExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        IllegalBlockSizeException e = new IllegalBlockSizeException("msg");
        check("msg".equals(e.getMessage()), "message ctor");
        check(e instanceof java.security.GeneralSecurityException, "extends java.security.GeneralSecurityException");
        check(e instanceof java.security.GeneralSecurityException, "is java.security.GeneralSecurityException");
        check(new IllegalBlockSizeException().getMessage() == null, "no-arg message null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
