/*
 * @test
 * @summary java.security.cert.CertificateRevokedException conformance: constructor and getters.
 * @run main CertificateRevokedExceptionTest
 */

public class CertificateRevokedExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.util.Date d = new java.util.Date(1000L);
        java.security.cert.CertificateRevokedException e = new java.security.cert.CertificateRevokedException(
            d, java.security.cert.CRLReason.KEY_COMPROMISE,
            new javax.security.auth.x500.X500Principal("CN=Test"), new java.util.HashMap<String, java.security.cert.Extension>());
        check(d.equals(e.getRevocationDate()), "getRevocationDate");
        check(java.security.cert.CRLReason.KEY_COMPROMISE.equals(e.getRevocationReason()), "getRevocationReason");
        check(e.getAuthorityName() != null, "getAuthorityName");
        check(e.getInvalidityDate() == null, "getInvalidityDate null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
