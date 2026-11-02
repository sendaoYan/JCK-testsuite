/*
 * @test
 * @summary java.security.cert.CertPathValidatorException conformance: constructor and accessors.
 * @run main CertPathValidatorExceptionTest
 */

import java.security.cert.CertPathValidatorException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertPath;
import java.util.Collections;

public class CertPathValidatorExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        CertPath cp = CertificateFactory.getInstance("X.509").generateCertPath(Collections.emptyList());
        Exception cause = new Exception("cause");
        CertPathValidatorException e = new CertPathValidatorException("msg", cause, cp, -1, CertPathValidatorException.BasicReason.EXPIRED);
        check(cp.equals(e.getCertPath()), "getCertPath");
        check(e.getIndex() == -1, "getIndex");
        check(CertPathValidatorException.BasicReason.EXPIRED == e.getReason(), "getReason");
        check("msg".equals(e.getMessage()), "getMessage");
        check(cause == e.getCause(), "getCause");
        check(e instanceof java.security.GeneralSecurityException, "extends GeneralSecurityException");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
