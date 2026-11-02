/*
 * @test
 * @summary java.security.spec.RSAKeyGenParameterSpec conformance: constructor and accessors.
 * @run main RSAKeyGenParameterSpecTest
 */

import java.security.spec.RSAKeyGenParameterSpec;

public class RSAKeyGenParameterSpecTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        RSAKeyGenParameterSpec spec = new RSAKeyGenParameterSpec(2048, RSAKeyGenParameterSpec.F4);
        check(spec.getKeysize() == 2048, "getKeysize");
        check(RSAKeyGenParameterSpec.F4.equals(spec.getPublicExponent()), "getPublicExponent");
        check(spec.getKeyParams() == null, "getKeyParams null");
        check(RSAKeyGenParameterSpec.F0.equals(java.math.BigInteger.valueOf(3)), "F0 == 3");
        check(RSAKeyGenParameterSpec.F4.equals(java.math.BigInteger.valueOf(65537)), "F4 == 65537");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
