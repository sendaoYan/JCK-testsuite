/*
 * @test
 * @summary javax.sql.rowset.spi.SyncFactory conformance: registered providers and logger
 * @run main SyncFactoryTest
 */

public class SyncFactoryTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        javax.sql.rowset.spi.SyncFactory sf = javax.sql.rowset.spi.SyncFactory.getSyncFactory();
        check(sf != null, "getSyncFactory non-null");
        check(javax.sql.rowset.spi.SyncFactory.ROWSET_SYNC_PROVIDER.equals("rowset.provider.classname"), "ROWSET_SYNC_PROVIDER");
        check(javax.sql.rowset.spi.SyncFactory.ROWSET_SYNC_VENDOR.equals("rowset.provider.vendor"), "ROWSET_SYNC_VENDOR");
        check(javax.sql.rowset.spi.SyncFactory.ROWSET_SYNC_PROVIDER_VERSION.equals("rowset.provider.version"), "ROWSET_SYNC_PROVIDER_VERSION");
        java.util.Enumeration<javax.sql.rowset.spi.SyncProvider> e = javax.sql.rowset.spi.SyncFactory.getRegisteredProviders();
        check(e != null, "getRegisteredProviders non-null");
        int n = 0;
        String id = null;
        while (e.hasMoreElements()) {
            javax.sql.rowset.spi.SyncProvider p = e.nextElement();
            check(p.getProviderID() != null, "provider id non-null");
            if (id == null) id = p.getProviderID();
            n++;
        }
        check(n >= 1, "at least one registered provider");
        check(id != null && javax.sql.rowset.spi.SyncFactory.getInstance(id) != null, "getInstance roundtrip");
        java.util.logging.Logger lg = java.util.logging.Logger.getLogger("SyncFactoryTest");
        javax.sql.rowset.spi.SyncFactory.setLogger(lg);
        check(javax.sql.rowset.spi.SyncFactory.getLogger() == lg, "getLogger after setLogger");        System.out.println("PASS (" + passed + " assertions)");
    }
}
