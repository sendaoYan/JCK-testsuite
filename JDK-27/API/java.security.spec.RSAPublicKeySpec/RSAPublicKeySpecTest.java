/*
 * @test
 * @summary java.security.spec.RSAPublicKeySpec conformance: constructor and accessors.
 * @run main RSAPublicKeySpecTest
 */

import java.security.spec.RSAPublicKeySpec;
import java.math.BigInteger;

public class RSAPublicKeySpecTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        BigInteger mod = new BigInteger("33"), exp = new BigInteger("3");
        RSAPublicKeySpec spec = new RSAPublicKeySpec(mod, exp);
        check(mod.equals(spec.getModulus()), "getModulus");
        check(exp.equals(spec.getPublicExponent()), "getPublicExponent");
        check(spec.getParams() == null, "getParams null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
