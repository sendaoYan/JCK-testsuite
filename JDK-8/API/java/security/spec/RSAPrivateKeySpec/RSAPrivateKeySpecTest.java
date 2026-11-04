/*
 * @test
 * @summary java.security.spec.RSAPrivateKeySpec conformance: constructor and getters.
 * @run main RSAPrivateKeySpecTest
 */

public class RSAPrivateKeySpecTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.security.spec.RSAPrivateKeySpec s = new java.security.spec.RSAPrivateKeySpec(
            java.math.BigInteger.valueOf(35), java.math.BigInteger.valueOf(5));
        check(java.math.BigInteger.valueOf(35).equals(s.getModulus()), "getModulus");
        check(java.math.BigInteger.valueOf(5).equals(s.getPrivateExponent()), "getPrivateExponent");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
