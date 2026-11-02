/*
 * @test
 * @summary java.security.Provider.Service conformance: constructor and getters.
 * @run main ServiceTest
 */

public class ServiceTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.security.Provider p = java.security.Security.getProviders()[0];
        java.security.Provider.Service s = new java.security.Provider.Service(
            p, "MessageDigest", "SHA-256", "java.lang.String",
            java.util.Arrays.asList("SHA256"), new java.util.HashMap<String, String>());
        check("MessageDigest".equals(s.getType()), "getType");
        check("SHA-256".equals(s.getAlgorithm()), "getAlgorithm");
        check(p.equals(s.getProvider()), "getProvider");
        check("java.lang.String".equals(s.getClassName()), "getClassName");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
