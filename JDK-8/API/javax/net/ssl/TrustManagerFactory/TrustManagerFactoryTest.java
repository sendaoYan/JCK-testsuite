/*
 * @test
 * @summary javax.net.ssl.TrustManagerFactory conformance: getInstance/init/getTrustManagers.
 * @run main TrustManagerFactoryTest
 */

public class TrustManagerFactoryTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.net.ssl.TrustManagerFactory tmf = javax.net.ssl.TrustManagerFactory.getInstance(
            javax.net.ssl.TrustManagerFactory.getDefaultAlgorithm());
        java.security.KeyStore ks = java.security.KeyStore.getInstance(java.security.KeyStore.getDefaultType());
        ks.load(null, null);
        tmf.init(ks);
        check(tmf.getTrustManagers().length >= 1, "getTrustManagers");
        check(tmf.getAlgorithm() != null, "getAlgorithm");
        check(tmf.getProvider() != null, "getProvider");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
