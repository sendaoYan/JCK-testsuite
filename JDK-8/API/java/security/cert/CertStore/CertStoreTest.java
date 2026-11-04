/*
 * @test
 * @summary java.security.cert.CertStore conformance: getInstance/getType/getProvider.
 * @run main CertStoreTest
 */

public class CertStoreTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.security.cert.CertStore s = java.security.cert.CertStore.getInstance("Collection",
            new java.security.cert.CollectionCertStoreParameters());
        check("Collection".equals(s.getType()), "getType");
        check(s.getProvider() != null, "getProvider");
        check(s.getCertStoreParameters() != null, "getCertStoreParameters");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
