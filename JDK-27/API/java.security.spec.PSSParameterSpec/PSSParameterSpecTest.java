/*
 * @test
 * @summary java.security.spec.PSSParameterSpec conformance: constructor and accessors.
 * @run main PSSParameterSpecTest
 */

import java.security.spec.PSSParameterSpec;

public class PSSParameterSpecTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        PSSParameterSpec spec = new PSSParameterSpec(32);
        check(spec.getSaltLength() == 32, "getSaltLength");
        check("SHA-1".equals(spec.getDigestAlgorithm()), "getDigestAlgorithm default");
        check("MGF1".equals(spec.getMGFAlgorithm()), "getMGFAlgorithm");
        check(spec.getTrailerField() == PSSParameterSpec.TRAILER_FIELD_BC, "getTrailerField BC");
        PSSParameterSpec full = new PSSParameterSpec("SHA-512", "MGF1", new java.security.spec.MGF1ParameterSpec("SHA-512"), 64, 1);
        check(full.getSaltLength() == 64, "full getSaltLength");
        check("SHA-512".equals(full.getDigestAlgorithm()), "full getDigestAlgorithm");
        check(PSSParameterSpec.DEFAULT != null, "DEFAULT non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
