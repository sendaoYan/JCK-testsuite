/*
 * @test
 * @summary javax.crypto.AEADBadTagException conformance: AEAD tag mismatch exception hierarchy.
 * @run main AEADBadTagExceptionTest
 */

import javax.crypto.AEADBadTagException;

public class AEADBadTagExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        AEADBadTagException e = new AEADBadTagException("msg");
        check("msg".equals(e.getMessage()), "message ctor");
        check(e instanceof javax.crypto.BadPaddingException, "extends javax.crypto.BadPaddingException");
        check(e instanceof java.security.GeneralSecurityException, "is java.security.GeneralSecurityException");
        check(new AEADBadTagException().getMessage() == null, "no-arg message null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
