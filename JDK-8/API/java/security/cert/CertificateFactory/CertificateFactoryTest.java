/*
 * @test
 * @summary java.security.cert.CertificateFactory conformance: getInstance/getType/getProvider.
 * @run main CertificateFactoryTest
 */

public class CertificateFactoryTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.security.cert.CertificateFactory f = java.security.cert.CertificateFactory.getInstance("X.509");
        check("X.509".equals(f.getType()), "getType");
        check(f.getProvider() != null, "getProvider");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
