/*
 * @test
 * @summary java.security.cert.CertificateRevokedException conformance: constructor and accessors.
 * @run main CertificateRevokedExceptionTest
 */

import java.security.cert.CertificateRevokedException;
import java.security.cert.CRLReason;
import java.util.Date;
import java.util.HashMap;
import javax.security.auth.x500.X500Principal;

public class CertificateRevokedExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        Date d = new Date();
        X500Principal ca = new X500Principal("CN=CA");
        CertificateRevokedException e = new CertificateRevokedException(d, CRLReason.KEY_COMPROMISE, ca, new HashMap<>());
        check(d.equals(e.getRevocationDate()), "getRevocationDate");
        check(CRLReason.KEY_COMPROMISE == e.getRevocationReason(), "getRevocationReason");
        check(ca.equals(e.getAuthorityName()), "getAuthorityName");
        check(e.getExtensions().isEmpty(), "getExtensions empty");
        check(e.getMessage() != null, "getMessage non-null");
        check(e instanceof java.security.cert.CertificateException, "extends CertificateException");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
