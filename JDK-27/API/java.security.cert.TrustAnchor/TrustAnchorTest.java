/*
 * @test
 * @summary java.security.cert.TrustAnchor conformance: constructor with name/public key.
 * @run main TrustAnchorTest
 */

import java.security.cert.TrustAnchor;
import java.security.KeyPairGenerator;
import java.security.PublicKey;

public class TrustAnchorTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        PublicKey pub = KeyPairGenerator.getInstance("RSA").generateKeyPair().getPublic();
        TrustAnchor ta = new TrustAnchor("CN=Test", pub, null);
        check("CN=Test".equals(ta.getCAName()), "getCAName");
        check(pub.equals(ta.getCAPublicKey()), "getCAPublicKey");
        check(ta.getNameConstraints() == null, "getNameConstraints null");
        check(ta.getTrustedCert() == null, "getTrustedCert null");
        check(ta.getCA() != null, "getCA non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
