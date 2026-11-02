/*
 * @test
 * @summary java.security.spec.XECPublicKeySpec conformance: constructor and accessors.
 * @run main XECPublicKeySpecTest
 */

import java.security.spec.XECPublicKeySpec;
import java.security.spec.NamedParameterSpec;
import java.math.BigInteger;

public class XECPublicKeySpecTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        XECPublicKeySpec spec = new XECPublicKeySpec(NamedParameterSpec.X25519, BigInteger.TEN);
        check(NamedParameterSpec.X25519.equals(spec.getParams()), "getParams");
        check(BigInteger.TEN.equals(spec.getU()), "getU");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
