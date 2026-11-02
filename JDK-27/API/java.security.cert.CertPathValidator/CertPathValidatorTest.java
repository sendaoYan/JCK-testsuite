/*
 * @test
 * @summary java.security.cert.CertPathValidator conformance: getInstance, getDefaultType, getAlgorithm/getProvider.
 * @run main CertPathValidatorTest
 */

import java.security.cert.CertPathValidator;

public class CertPathValidatorTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        String def = CertPathValidator.getDefaultType();
        check(def != null, "getDefaultType non-null");
        CertPathValidator cpv = CertPathValidator.getInstance(def);
        check(cpv.getAlgorithm() != null, "getAlgorithm");
        check(cpv.getProvider() != null, "getProvider");
        check(cpv.getRevocationChecker() != null, "getRevocationChecker");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
