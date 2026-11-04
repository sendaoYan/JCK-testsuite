/*
 * @test
 * @summary java.security.spec.PSSParameterSpec conformance: constructors and getters.
 * @run main PSSParameterSpecTest
 */

public class PSSParameterSpecTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.security.spec.PSSParameterSpec s = new java.security.spec.PSSParameterSpec(32);
        check(s.getSaltLength() == 32, "getSaltLength");
        java.security.spec.PSSParameterSpec s2 = new java.security.spec.PSSParameterSpec(
            "SHA-256", "MGF1", new java.security.spec.MGF1ParameterSpec("SHA-256"), 32, 1);
        check("SHA-256".equals(s2.getDigestAlgorithm()), "getDigestAlgorithm");
        check("MGF1".equals(s2.getMGFAlgorithm()), "getMGFAlgorithm");
        check(s2.getSaltLength() == 32, "getSaltLength 2");
        check(s2.getTrailerField() == 1, "getTrailerField");
        check(java.security.spec.PSSParameterSpec.DEFAULT.getSaltLength() == 20, "DEFAULT");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
