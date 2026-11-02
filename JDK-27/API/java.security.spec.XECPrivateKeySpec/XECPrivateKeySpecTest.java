/*
 * @test
 * @summary java.security.spec.XECPrivateKeySpec conformance: constructor and accessors.
 * @run main XECPrivateKeySpecTest
 */

import java.security.spec.XECPrivateKeySpec;
import java.security.spec.NamedParameterSpec;

public class XECPrivateKeySpecTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        byte[] scalar = new byte[32];
        XECPrivateKeySpec spec = new XECPrivateKeySpec(NamedParameterSpec.X25519, scalar);
        check(NamedParameterSpec.X25519.equals(spec.getParams()), "getParams");
        check(java.util.Arrays.equals(scalar, spec.getScalar()), "getScalar");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
