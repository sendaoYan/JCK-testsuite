/*
 * @test
 * @summary java.security.cert.CertPathValidator conformance: getInstance/getAlgorithm/getProvider/getRevocationChecker.
 * @run main CertPathValidatorTest
 */

public class CertPathValidatorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.security.cert.CertPathValidator v = java.security.cert.CertPathValidator.getInstance("PKIX");
        check("PKIX".equals(v.getAlgorithm()), "getAlgorithm");
        check(v.getProvider() != null, "getProvider");
        check(v.getRevocationChecker() != null, "getRevocationChecker");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
