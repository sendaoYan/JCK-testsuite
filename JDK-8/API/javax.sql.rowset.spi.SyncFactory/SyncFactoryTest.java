/*
 * @test
 * @summary javax.sql.rowset.spi.SyncFactory conformance: registered providers.
 * @run main SyncFactoryTest
 */


public class SyncFactoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        java.util.Enumeration<javax.sql.rowset.spi.SyncProvider> provs = javax.sql.rowset.spi.SyncFactory.getRegisteredProviders();
        check(provs != null, "getRegisteredProviders non-null");
        check("rowset.provider.version".equals(javax.sql.rowset.spi.SyncFactory.ROWSET_SYNC_PROVIDER_VERSION), "provider version key");        System.out.println("PASS (" + passed + " assertions)");
    }
}
