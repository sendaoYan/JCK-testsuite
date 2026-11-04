/*
 * @test
 * @summary javax.sql.rowset.spi.SyncProvider conformance: white-box structure + black-box behavior.
 * @run main SyncProviderTest
 */

import javax.sql.RowSetReader;
import javax.sql.RowSetWriter;
import javax.sql.rowset.spi.SyncFactory;
import javax.sql.rowset.spi.SyncProvider;
import javax.sql.rowset.spi.SyncProviderException;

public class SyncProviderTest {
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
        Class<?> c = Class.forName("javax.sql.rowset.spi.SyncProvider");
        check(!c.isInterface() && java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract class");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass Object");
        check(c.getInterfaces().length == 0, "no interfaces");
        check(c.getDeclaredMethods().length == 9, "declared method count 9");

        check(SyncProvider.GRADE_NONE == 1, "GRADE_NONE == 1");
        check(SyncProvider.GRADE_CHECK_MODIFIED_AT_COMMIT == 2, "GRADE_CHECK_MODIFIED_AT_COMMIT == 2");
        check(SyncProvider.GRADE_CHECK_ALL_AT_COMMIT == 3, "GRADE_CHECK_ALL_AT_COMMIT == 3");
        check(SyncProvider.GRADE_LOCK_WHEN_MODIFIED == 4, "GRADE_LOCK_WHEN_MODIFIED == 4");
        check(SyncProvider.GRADE_LOCK_WHEN_LOADED == 5, "GRADE_LOCK_WHEN_LOADED == 5");
        check(SyncProvider.DATASOURCE_NO_LOCK == 1, "DATASOURCE_NO_LOCK == 1");
        check(SyncProvider.DATASOURCE_ROW_LOCK == 2, "DATASOURCE_ROW_LOCK == 2");
        check(SyncProvider.DATASOURCE_TABLE_LOCK == 3, "DATASOURCE_TABLE_LOCK == 3");
        check(SyncProvider.DATASOURCE_DB_LOCK == 4, "DATASOURCE_DB_LOCK == 4");
        check(SyncProvider.UPDATABLE_VIEW_SYNC == 5, "UPDATABLE_VIEW_SYNC == 5");
        check(SyncProvider.NONUPDATABLE_VIEW_SYNC == 6, "NONUPDATABLE_VIEW_SYNC == 6");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // A stateful anonymous SyncProvider exercises the abstract accessor round-trip.
        SyncProvider p = new SyncProvider() {
            int lock = SyncProvider.DATASOURCE_NO_LOCK;
            public String getProviderID() { return "test.provider"; }
            public RowSetReader getRowSetReader() { return null; }
            public RowSetWriter getRowSetWriter() { return null; }
            public int getProviderGrade() { return SyncProvider.GRADE_CHECK_ALL_AT_COMMIT; }
            public void setDataSourceLock(int datasourceLock) { this.lock = datasourceLock; }
            public int getDataSourceLock() { return this.lock; }
            public int supportsUpdatableView() { return SyncProvider.UPDATABLE_VIEW_SYNC; }
            public String getVersion() { return "9.9"; }
            public String getVendor() { return "Test Vendor"; }
        };
        check("test.provider".equals(p.getProviderID()), "anonymous getProviderID");
        check("Test Vendor".equals(p.getVendor()), "anonymous getVendor");
        check("9.9".equals(p.getVersion()), "anonymous getVersion");
        check(p.getProviderGrade() == SyncProvider.GRADE_CHECK_ALL_AT_COMMIT, "anonymous getProviderGrade");
        check(p.supportsUpdatableView() == SyncProvider.UPDATABLE_VIEW_SYNC, "anonymous supportsUpdatableView");
        check(p.getRowSetReader() == null, "anonymous getRowSetReader null");
        check(p.getRowSetWriter() == null, "anonymous getRowSetWriter null");
        check(p.getDataSourceLock() == SyncProvider.DATASOURCE_NO_LOCK, "anonymous default lock");
        p.setDataSourceLock(SyncProvider.DATASOURCE_ROW_LOCK);
        check(p.getDataSourceLock() == SyncProvider.DATASOURCE_ROW_LOCK, "anonymous setDataSourceLock round-trip");

        // Reference optimistic provider behavior: lock semantics reject unsupported locks.
        SyncProvider ri = SyncFactory.getInstance("com.sun.rowset.providers.RIOptimisticProvider");
        check(ri.getProviderGrade() == SyncProvider.GRADE_CHECK_MODIFIED_AT_COMMIT, "RI grade");
        check(ri.getDataSourceLock() == SyncProvider.DATASOURCE_NO_LOCK, "RI default lock");
        check(ri.supportsUpdatableView() == SyncProvider.NONUPDATABLE_VIEW_SYNC, "RI view sync");
        checkThrows(SyncProviderException.class, () -> ri.setDataSourceLock(SyncProvider.DATASOURCE_ROW_LOCK), "RI rejects non-NO_LOCK");
        checkThrows(SyncProviderException.class, () -> ri.setDataSourceLock(SyncProvider.DATASOURCE_TABLE_LOCK), "RI rejects TABLE_LOCK");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
