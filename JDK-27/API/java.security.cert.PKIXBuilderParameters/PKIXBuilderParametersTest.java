/*
 * @test
 * @summary java.security.cert.PKIXBuilderParameters conformance: constructor and maxPathLength.
 * @run main PKIXBuilderParametersTest
 */

import java.security.cert.PKIXBuilderParameters;
import java.security.cert.TrustAnchor;
import java.security.KeyPairGenerator;
import java.util.HashSet;

public class PKIXBuilderParametersTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        HashSet<TrustAnchor> anchors = new HashSet<>();
        anchors.add(new TrustAnchor("CN=Test", KeyPairGenerator.getInstance("RSA").generateKeyPair().getPublic(), null));
        PKIXBuilderParameters params = new PKIXBuilderParameters(anchors, null);
        check(params.getTrustAnchors().size() == 1, "getTrustAnchors");
        check(params.getMaxPathLength() == 5, "default maxPathLength 5");
        params.setMaxPathLength(3);
        check(params.getMaxPathLength() == 3, "setMaxPathLength/getMaxPathLength");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
