/*
 * @test
 * @summary javax.crypto.ExemptionMechanismException conformance: exemption mechanism exception hierarchy.
 * @run main ExemptionMechanismExceptionTest
 */

import javax.crypto.ExemptionMechanismException;

public class ExemptionMechanismExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        ExemptionMechanismException e = new ExemptionMechanismException("msg");
        check("msg".equals(e.getMessage()), "message ctor");
        check(e instanceof java.security.GeneralSecurityException, "extends java.security.GeneralSecurityException");
        check(e instanceof java.security.GeneralSecurityException, "is java.security.GeneralSecurityException");
        check(new ExemptionMechanismException().getMessage() == null, "no-arg message null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
