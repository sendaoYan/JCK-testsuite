/*
 * @test
 * @summary java.security.cert.PKIXCertPathValidatorResult conformance: constructor and accessors.
 * @run main PKIXCertPathValidatorResultTest
 */

import java.security.cert.PKIXCertPathValidatorResult;
import java.security.cert.TrustAnchor;
import java.security.KeyPairGenerator;
import java.security.PublicKey;

public class PKIXCertPathValidatorResultTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        PublicKey pub = KeyPairGenerator.getInstance("RSA").generateKeyPair().getPublic();
        TrustAnchor ta = new TrustAnchor("CN=Test", pub, null);
        PKIXCertPathValidatorResult r = new PKIXCertPathValidatorResult(ta, null, pub);
        check(ta.equals(r.getTrustAnchor()), "getTrustAnchor");
        check(r.getPolicyTree() == null, "getPolicyTree null");
        check(pub.equals(r.getPublicKey()), "getPublicKey");
        check(r instanceof java.security.cert.CertPathValidatorResult, "implements CertPathValidatorResult");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
