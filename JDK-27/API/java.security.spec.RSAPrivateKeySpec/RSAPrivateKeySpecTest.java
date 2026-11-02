/*
 * @test
 * @summary java.security.spec.RSAPrivateKeySpec conformance: constructor and accessors.
 * @run main RSAPrivateKeySpecTest
 */

import java.security.spec.RSAPrivateKeySpec;
import java.math.BigInteger;

public class RSAPrivateKeySpecTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        BigInteger mod = new BigInteger("33"), exp = new BigInteger("7");
        RSAPrivateKeySpec spec = new RSAPrivateKeySpec(mod, exp);
        check(mod.equals(spec.getModulus()), "getModulus");
        check(exp.equals(spec.getPrivateExponent()), "getPrivateExponent");
        check(spec.getParams() == null, "getParams null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
