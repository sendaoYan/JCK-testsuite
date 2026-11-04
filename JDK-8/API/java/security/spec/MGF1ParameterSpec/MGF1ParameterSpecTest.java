/*
 * @test
 * @summary java.security.spec.MGF1ParameterSpec conformance: constructor and constants.
 * @run main MGF1ParameterSpecTest
 */

public class MGF1ParameterSpecTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.security.spec.MGF1ParameterSpec s = new java.security.spec.MGF1ParameterSpec("SHA-256");
        check("SHA-256".equals(s.getDigestAlgorithm()), "getDigestAlgorithm");
        check("SHA-256".equals(java.security.spec.MGF1ParameterSpec.SHA256.getDigestAlgorithm()), "SHA256 constant");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
