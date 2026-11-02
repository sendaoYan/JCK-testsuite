/*
 * @test
 * @summary javax.net.ssl.KeyStoreBuilderParameters conformance: constructor and getParameters.
 * @run main KeyStoreBuilderParametersTest
 */

public class KeyStoreBuilderParametersTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.util.List<java.security.KeyStore.Builder> list = new java.util.ArrayList<java.security.KeyStore.Builder>();
        list.add(null);
        javax.net.ssl.KeyStoreBuilderParameters p = new javax.net.ssl.KeyStoreBuilderParameters(list);
        check(p.getParameters() != null && p.getParameters().size() == 1, "getParameters");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
