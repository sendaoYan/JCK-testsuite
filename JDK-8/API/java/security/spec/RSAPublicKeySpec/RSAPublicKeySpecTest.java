/*
 * @test
 * @summary java.security.spec.RSAPublicKeySpec conformance: constructor and getters.
 * @run main RSAPublicKeySpecTest
 */

public class RSAPublicKeySpecTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.security.spec.RSAPublicKeySpec s = new java.security.spec.RSAPublicKeySpec(
            java.math.BigInteger.valueOf(35), java.math.BigInteger.valueOf(7));
        check(java.math.BigInteger.valueOf(35).equals(s.getModulus()), "getModulus");
        check(java.math.BigInteger.valueOf(7).equals(s.getPublicExponent()), "getPublicExponent");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
