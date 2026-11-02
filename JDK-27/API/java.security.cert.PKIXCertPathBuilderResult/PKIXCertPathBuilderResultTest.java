/*
 * @test
 * @summary java.security.cert.PKIXCertPathBuilderResult conformance: constructor and accessors.
 * @run main PKIXCertPathBuilderResultTest
 */

import java.security.cert.PKIXCertPathBuilderResult;
import java.security.cert.TrustAnchor;
import java.security.cert.CertificateFactory;
import java.security.cert.CertPath;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.util.Collections;

public class PKIXCertPathBuilderResultTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        PublicKey pub = KeyPairGenerator.getInstance("RSA").generateKeyPair().getPublic();
        TrustAnchor ta = new TrustAnchor("CN=Test", pub, null);
        CertPath cp = CertificateFactory.getInstance("X.509").generateCertPath(Collections.emptyList());
        PKIXCertPathBuilderResult r = new PKIXCertPathBuilderResult(cp, ta, null, pub);
        check(cp.equals(r.getCertPath()), "getCertPath");
        check(ta.equals(r.getTrustAnchor()), "getTrustAnchor");
        check(pub.equals(r.getPublicKey()), "getPublicKey");
        check(r instanceof java.security.cert.CertPathBuilderResult, "implements CertPathBuilderResult");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
