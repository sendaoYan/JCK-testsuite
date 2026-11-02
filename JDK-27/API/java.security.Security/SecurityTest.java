/*
 * @test
 * @summary java.security.Security conformance: getAlgorithms, getProviders, getProperty.
 * @run main SecurityTest
 */

import java.security.Security;

public class SecurityTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        check(Security.getProviders().length > 0, "getProviders non-empty");
        check(Security.getProvider("SUN") != null, "getProvider SUN");
        check(Security.getAlgorithms("MessageDigest").contains("SHA-256"), "getAlgorithms has SHA-256");
        check(Security.getProperty("security.provider.1") != null, "getProperty security.provider.1");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
