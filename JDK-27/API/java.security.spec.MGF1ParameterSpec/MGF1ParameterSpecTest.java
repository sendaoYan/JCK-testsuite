/*
 * @test
 * @summary java.security.spec.MGF1ParameterSpec conformance: constructor, getDigestAlgorithm, constants.
 * @run main MGF1ParameterSpecTest
 */

import java.security.spec.MGF1ParameterSpec;

public class MGF1ParameterSpecTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        MGF1ParameterSpec spec = new MGF1ParameterSpec("SHA-256");
        check("SHA-256".equals(spec.getDigestAlgorithm()), "getDigestAlgorithm");
        check("SHA-256".equals(MGF1ParameterSpec.SHA256.getDigestAlgorithm()), "SHA256 constant");
        check("SHA-1".equals(MGF1ParameterSpec.SHA1.getDigestAlgorithm()), "SHA1 constant");
        check("SHA-512".equals(MGF1ParameterSpec.SHA512.getDigestAlgorithm()), "SHA512 constant");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
