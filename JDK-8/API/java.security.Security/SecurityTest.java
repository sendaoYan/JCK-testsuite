/*
 * @test
 * @summary java.security.Security conformance: getProviders/getProperty/getAlgorithms.
 * @run main SecurityTest
 */

public class SecurityTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.security.Provider[] ps = java.security.Security.getProviders();
        check(ps.length > 0, "getProviders non-empty");
        check(java.security.Security.getProperty("security.provider.1") != null, "getProperty provider.1");
        java.util.Set<String> algs = java.security.Security.getAlgorithms("MessageDigest");
        check(algs != null && !algs.isEmpty(), "getAlgorithms MessageDigest");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
