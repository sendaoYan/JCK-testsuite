/*
 * @test
 * @summary javax.net.ssl.KeyManagerFactory conformance: getInstance/init/getKeyManagers.
 * @run main KeyManagerFactoryTest
 */

public class KeyManagerFactoryTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.net.ssl.KeyManagerFactory kmf = javax.net.ssl.KeyManagerFactory.getInstance(
            javax.net.ssl.KeyManagerFactory.getDefaultAlgorithm());
        java.security.KeyStore ks = java.security.KeyStore.getInstance(java.security.KeyStore.getDefaultType());
        ks.load(null, null);
        kmf.init(ks, "password".toCharArray());
        check(kmf.getKeyManagers().length >= 1, "getKeyManagers");
        check(kmf.getAlgorithm() != null, "getAlgorithm");
        check(kmf.getProvider() != null, "getProvider");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
