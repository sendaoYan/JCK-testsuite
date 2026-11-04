/*
 * @test
 * @summary javax.net.ssl.CertPathTrustManagerParameters conformance: constructor and getParameters.
 * @run main CertPathTrustManagerParametersTest
 */

public class CertPathTrustManagerParametersTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.security.KeyPairGenerator kpg = java.security.KeyPairGenerator.getInstance("RSA");
        kpg.initialize(512);
        java.security.PublicKey pk = kpg.generateKeyPair().getPublic();
        java.util.Set<java.security.cert.TrustAnchor> anchors = new java.util.HashSet<java.security.cert.TrustAnchor>();
        anchors.add(new java.security.cert.TrustAnchor("CN=Test", pk, null));
        java.security.cert.CertPathParameters cpp = new java.security.cert.PKIXParameters(anchors);
        javax.net.ssl.CertPathTrustManagerParameters p = new javax.net.ssl.CertPathTrustManagerParameters(cpp);
        check(p.getParameters() != null, "getParameters");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
