/*
 * @test
 * @summary javax.crypto.ExemptionMechanismException conformance: constructors, message, cause and superclass.
 * @run main ExemptionMechanismExceptionTest
 */

public class ExemptionMechanismExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.crypto.ExemptionMechanismException e = new javax.crypto.ExemptionMechanismException("boom");
        check("boom".equals(e.getMessage()), "String ctor sets message");
        check(e instanceof java.security.GeneralSecurityException, "extends java.security.GeneralSecurityException");
        javax.crypto.ExemptionMechanismException e4 = new javax.crypto.ExemptionMechanismException();
        check(e4.getMessage() == null, "no-arg ctor message null");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
