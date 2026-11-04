/*
 * @test
 * @summary java.security.DomainLoadStoreParameter conformance: constructor and getters.
 * @run main DomainLoadStoreParameterTest
 */

public class DomainLoadStoreParameterTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.security.DomainLoadStoreParameter p = new java.security.DomainLoadStoreParameter(
            new java.net.URI("file:/tmp/x"), new java.util.HashMap<String, java.security.KeyStore.ProtectionParameter>());
        check(p.getConfiguration() != null, "getConfiguration");
        check(p.getProtectionParams() != null, "getProtectionParams");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
