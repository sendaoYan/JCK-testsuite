/*
 * @test
 * @summary java.security.spec.RSAKeyGenParameterSpec conformance: constructor and getters.
 * @run main RSAKeyGenParameterSpecTest
 */

public class RSAKeyGenParameterSpecTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.security.spec.RSAKeyGenParameterSpec s = new java.security.spec.RSAKeyGenParameterSpec(2048, java.math.BigInteger.valueOf(65537));
        check(s.getKeysize() == 2048, "getKeysize");
        check(java.math.BigInteger.valueOf(65537).equals(s.getPublicExponent()), "getPublicExponent");
        check(s.getKeyParams() == null, "getKeyParams null");
        check(java.security.spec.RSAKeyGenParameterSpec.F4.intValue() == 65537, "F4 constant");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
