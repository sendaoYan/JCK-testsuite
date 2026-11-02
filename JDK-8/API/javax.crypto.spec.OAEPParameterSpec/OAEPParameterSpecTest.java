/*
 * @test
 * @summary javax.crypto.spec.OAEPParameterSpec conformance: constructor and getters.
 * @run main OAEPParameterSpecTest
 */

public class OAEPParameterSpecTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.crypto.spec.OAEPParameterSpec s = new javax.crypto.spec.OAEPParameterSpec(
            "SHA-256", "MGF1", new java.security.spec.MGF1ParameterSpec("SHA-256"),
            javax.crypto.spec.PSource.PSpecified.DEFAULT);
        check("SHA-256".equals(s.getDigestAlgorithm()), "getDigestAlgorithm");
        check("MGF1".equals(s.getMGFAlgorithm()), "getMGFAlgorithm");
        check(s.getMGFParameters() != null, "getMGFParameters");
        check(s.getPSource() != null, "getPSource");
        check(javax.crypto.spec.OAEPParameterSpec.DEFAULT != null, "DEFAULT");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
