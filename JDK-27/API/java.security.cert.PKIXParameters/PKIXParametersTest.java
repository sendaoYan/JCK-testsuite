/*
 * @test
 * @summary java.security.cert.PKIXParameters conformance: constructor and flag accessors.
 * @run main PKIXParametersTest
 */

import java.security.cert.PKIXParameters;
import java.security.cert.TrustAnchor;
import java.security.KeyPairGenerator;
import java.util.HashSet;
import java.util.Date;

public class PKIXParametersTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        HashSet<TrustAnchor> anchors = new HashSet<>();
        anchors.add(new TrustAnchor("CN=Test", KeyPairGenerator.getInstance("RSA").generateKeyPair().getPublic(), null));
        PKIXParameters params = new PKIXParameters(anchors);
        check(params.getTrustAnchors().size() == 1, "getTrustAnchors");
        check(params.isRevocationEnabled(), "revocation enabled default");
        params.setRevocationEnabled(false);
        check(!params.isRevocationEnabled(), "setRevocationEnabled");
        check(params.getDate() == null, "getDate null default");
        Date d = new Date();
        params.setDate(d);
        check(d.equals(params.getDate()), "setDate/getDate");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
