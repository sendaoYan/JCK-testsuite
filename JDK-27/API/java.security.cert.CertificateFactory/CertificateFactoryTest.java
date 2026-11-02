/*
 * @test
 * @summary java.security.cert.CertificateFactory conformance: X.509 getInstance, getType/getProvider/getCertPathEncodings.
 * @run main CertificateFactoryTest
 */

import java.security.cert.CertificateFactory;

public class CertificateFactoryTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        check("X.509".equals(cf.getType()), "getType");
        check(cf.getProvider() != null, "getProvider");
        check(cf.getCertPathEncodings() != null, "getCertPathEncodings");
        java.util.Iterator<String> it = cf.getCertPathEncodings();
        check(it.hasNext(), "has cert path encoding");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
