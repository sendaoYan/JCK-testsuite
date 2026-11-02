/*
 * @test
 * @summary java.security.spec.EdECPrivateKeySpec conformance: constructor and accessors.
 * @run main EdECPrivateKeySpecTest
 */

import java.security.spec.EdECPrivateKeySpec;
import java.security.spec.NamedParameterSpec;

public class EdECPrivateKeySpecTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        byte[] bytes = new byte[32];
        EdECPrivateKeySpec spec = new EdECPrivateKeySpec(NamedParameterSpec.ED25519, bytes);
        check(NamedParameterSpec.ED25519.equals(spec.getParams()), "getParams");
        check(java.util.Arrays.equals(bytes, spec.getBytes()), "getBytes");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
