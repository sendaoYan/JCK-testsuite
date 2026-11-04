/*
 * @test
 * @summary java.security.cert.CertPathBuilder conformance: getInstance/getAlgorithm/getProvider.
 * @run main CertPathBuilderTest
 */

public class CertPathBuilderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.security.cert.CertPathBuilder b = java.security.cert.CertPathBuilder.getInstance("PKIX");
        check("PKIX".equals(b.getAlgorithm()), "getAlgorithm");
        check(b.getProvider() != null, "getProvider");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
