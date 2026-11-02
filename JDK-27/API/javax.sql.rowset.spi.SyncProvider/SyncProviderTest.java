/*
 * @test
 * @summary javax.sql.rowset.spi.SyncProvider conformance: abstract class shape and provider grade constants
 * @run main SyncProviderTest
 */

import java.lang.reflect.Modifier;

public class SyncProviderTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.sql.rowset.spi.SyncProvider");
        check(!c.isInterface() && java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract class");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass Object");
        check(c.getInterfaces().length == 0, "no interfaces");
        check(c.getDeclaredMethods().length == 9, "declared method count 9");
        check(javax.sql.rowset.spi.SyncProvider.GRADE_NONE == 1, "GRADE_NONE == 1");
        check(javax.sql.rowset.spi.SyncProvider.GRADE_CHECK_MODIFIED_AT_COMMIT == 2, "GRADE_CHECK_MODIFIED_AT_COMMIT == 2");
        check(javax.sql.rowset.spi.SyncProvider.GRADE_CHECK_ALL_AT_COMMIT == 3, "GRADE_CHECK_ALL_AT_COMMIT == 3");
        check(javax.sql.rowset.spi.SyncProvider.GRADE_LOCK_WHEN_MODIFIED == 4, "GRADE_LOCK_WHEN_MODIFIED == 4");
        check(javax.sql.rowset.spi.SyncProvider.GRADE_LOCK_WHEN_LOADED == 5, "GRADE_LOCK_WHEN_LOADED == 5");
        check(javax.sql.rowset.spi.SyncProvider.DATASOURCE_NO_LOCK == 1, "DATASOURCE_NO_LOCK == 1");
        check(javax.sql.rowset.spi.SyncProvider.DATASOURCE_ROW_LOCK == 2, "DATASOURCE_ROW_LOCK == 2");
        check(javax.sql.rowset.spi.SyncProvider.DATASOURCE_TABLE_LOCK == 3, "DATASOURCE_TABLE_LOCK == 3");
        check(javax.sql.rowset.spi.SyncProvider.DATASOURCE_DB_LOCK == 4, "DATASOURCE_DB_LOCK == 4");
        check(javax.sql.rowset.spi.SyncProvider.UPDATABLE_VIEW_SYNC == 5, "UPDATABLE_VIEW_SYNC == 5");
        check(javax.sql.rowset.spi.SyncProvider.NONUPDATABLE_VIEW_SYNC == 6, "NONUPDATABLE_VIEW_SYNC == 6");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
