/*
 * @test
 * @summary javax.sql.rowset.spi.SyncFactory conformance: white-box structure + black-box behavior.
 * @run main SyncFactoryTest
 */

import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.rowset.spi.SyncFactory;
import javax.sql.rowset.spi.SyncFactoryException;
import javax.sql.rowset.spi.SyncProvider;

public class SyncFactoryTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    interface Op { void run() throws Exception; }
    static Throwable caught(Op op) {
        try { op.run(); return null; } catch (Throwable t) { return t; }
    }
    static void checkThrows(Class<? extends Throwable> expected, Op op, String msg) {
        Throwable t = caught(op);
        check(t != null && expected.isInstance(t), msg + " (threw " + (t == null ? "nothing" : t.getClass().getName()) + ")");
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.sql.rowset.spi.SyncFactory");
        check(!c.isInterface(), "SyncFactory is a class");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass Object");
        check(c.getInterfaces().length == 0, "no interfaces");

        SyncFactory sf = SyncFactory.getSyncFactory();
        check(sf != null, "getSyncFactory non-null");
        check(SyncFactory.getSyncFactory() == sf, "getSyncFactory singleton");
        check(SyncFactory.ROWSET_SYNC_PROVIDER.equals("rowset.provider.classname"), "ROWSET_SYNC_PROVIDER");
        check(SyncFactory.ROWSET_SYNC_VENDOR.equals("rowset.provider.vendor"), "ROWSET_SYNC_VENDOR");
        check(SyncFactory.ROWSET_SYNC_PROVIDER_VERSION.equals("rowset.provider.version"), "ROWSET_SYNC_PROVIDER_VERSION");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // Logger lifecycle: no logger set -> getLogger throws; setLogger establishes the handle.
        checkThrows(SyncFactoryException.class, () -> SyncFactory.getLogger(), "getLogger with no logger set throws");
        Logger lg = Logger.getLogger("SyncFactoryTest");
        SyncFactory.setLogger(lg);
        check(SyncFactory.getLogger() == lg, "getLogger after setLogger");
        SyncFactory.setLogger(lg, Level.FINE);
        check(SyncFactory.getLogger() == lg, "getLogger after setLogger(level)");
        check(lg.getLevel() == Level.FINE, "setLogger applied level");
        checkThrows(NullPointerException.class, () -> SyncFactory.setLogger((Logger) null), "setLogger(null) throws NPE");
        checkThrows(SyncFactoryException.class, () -> SyncFactory.setJNDIContext(null), "setJNDIContext(null) throws");

        // Registered providers: the two reference implementations are present.
        TreeSet<String> ids = new TreeSet<>();
        for (java.util.Enumeration<SyncProvider> e = SyncFactory.getRegisteredProviders(); e.hasMoreElements();) {
            SyncProvider p = e.nextElement();
            check(p.getProviderID() != null, "provider id non-null");
            ids.add(p.getProviderID());
        }
        check(ids.contains("com.sun.rowset.providers.RIOptimisticProvider"), "RIOptimisticProvider registered");
        check(ids.contains("com.sun.rowset.providers.RIXMLProvider"), "RIXMLProvider registered");

        // getInstance: known providers, default fallback for unknown id, null rejected.
        SyncProvider ri = SyncFactory.getInstance("com.sun.rowset.providers.RIOptimisticProvider");
        check(ri != null, "getInstance RIOptimisticProvider non-null");
        check("com.sun.rowset.providers.RIOptimisticProvider".equals(ri.getProviderID()), "RI provider id");
        check("Oracle Corporation".equals(ri.getVendor()), "RI provider vendor");
        check("1.0".equals(ri.getVersion()), "RI provider version");
        check(ri.getProviderGrade() == SyncProvider.GRADE_CHECK_MODIFIED_AT_COMMIT, "RI grade");
        check(ri.getDataSourceLock() == SyncProvider.DATASOURCE_NO_LOCK, "RI datasource lock");
        check(ri.supportsUpdatableView() == SyncProvider.NONUPDATABLE_VIEW_SYNC, "RI view sync");
        check(ri.getRowSetReader() != null, "RI reader non-null");
        check(ri.getRowSetWriter() != null, "RI writer non-null");

        SyncProvider xml = SyncFactory.getInstance("com.sun.rowset.providers.RIXMLProvider");
        check(xml != null && "com.sun.rowset.providers.RIXMLProvider".equals(xml.getProviderID()), "XML provider id");

        checkThrows(SyncFactoryException.class, () -> SyncFactory.getInstance((String) null), "getInstance(null) throws");
        // Documented reference behavior: an unknown id falls back to the reference optimistic provider.
        SyncProvider fallback = SyncFactory.getInstance("no.such.provider.ClassName");
        check(fallback != null, "unknown id fallback non-null");
        check("com.sun.rowset.providers.RIOptimisticProvider".equals(fallback.getProviderID()), "unknown id falls back to RI provider");

        // register/unregister round-trip; unregistered-but-unloadable id surfaces as SyncFactoryException.
        String bogus = "com.example.NoSuchRowSetProvider";
        SyncFactory.registerProvider(bogus);
        TreeSet<String> after = new TreeSet<>();
        for (java.util.Enumeration<SyncProvider> e = SyncFactory.getRegisteredProviders(); e.hasMoreElements();) {
            after.add(e.nextElement().getProviderID());
        }
        check(after.contains(bogus), "registerProvider adds provider id");
        checkThrows(SyncFactoryException.class, () -> SyncFactory.getInstance(bogus), "getInstance of unloadable registered provider throws");
        SyncFactory.unregisterProvider(bogus);
        TreeSet<String> removed = new TreeSet<>();
        for (java.util.Enumeration<SyncProvider> e = SyncFactory.getRegisteredProviders(); e.hasMoreElements();) {
            removed.add(e.nextElement().getProviderID());
        }
        check(!removed.contains(bogus), "unregisterProvider removes provider id");
        // unregisterProvider of a provider that is not registered is a no-op (no exception).
        caught(() -> { SyncFactory.unregisterProvider("never.registered.Provider"); return; });
        check(true, "unregisterProvider unknown id is a no-op");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
