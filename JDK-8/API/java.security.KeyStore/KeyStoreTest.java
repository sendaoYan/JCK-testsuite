/*
 * @test
 * @summary java.security.KeyStore conformance: getInstance/load/size/type/provider.
 * @run main KeyStoreTest
 */

public class KeyStoreTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.security.KeyStore ks = java.security.KeyStore.getInstance(java.security.KeyStore.getDefaultType());
        ks.load(null, null);
        check(ks.size() == 0, "size 0 after load(null,null)");
        check(ks.getType() != null, "getType");
        check(ks.getProvider() != null, "getProvider");
        check(ks.aliases() != null && !ks.aliases().hasMoreElements(), "aliases empty");
        check(!ks.containsAlias("nope"), "containsAlias false");
        check(java.security.KeyStore.getDefaultType() != null, "getDefaultType");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
