/*
 * @test
 * @summary java.security.spec.ECGenParameterSpec conformance: constructor and getName.
 * @run main ECGenParameterSpecTest
 */

import java.security.spec.ECGenParameterSpec;

public class ECGenParameterSpecTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        ECGenParameterSpec spec = new ECGenParameterSpec("secp256r1");
        check("secp256r1".equals(spec.getName()), "getName");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
