/*
 * @test
 * @summary java.security.cert.CertPathBuilder conformance: getInstance, getDefaultType, getAlgorithm/getProvider.
 * @run main CertPathBuilderTest
 */

import java.security.cert.CertPathBuilder;

public class CertPathBuilderTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        String def = CertPathBuilder.getDefaultType();
        check(def != null, "getDefaultType non-null");
        CertPathBuilder cpb = CertPathBuilder.getInstance(def);
        check(cpb.getAlgorithm() != null, "getAlgorithm");
        check(cpb.getProvider() != null, "getProvider");
        check(cpb.getRevocationChecker() != null, "getRevocationChecker");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
