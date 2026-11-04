/*
 * @test
 * @summary javax.sql.rowset.spi.SyncResolver conformance: white-box structure + black-box behavior.
 * @run main SyncResolverTest
 */

import java.sql.SQLException;
import javax.sql.RowSet;
import javax.sql.rowset.spi.SyncProviderException;
import javax.sql.rowset.spi.SyncResolver;

public class SyncResolverTest {
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
        Class<?> c = Class.forName("javax.sql.rowset.spi.SyncResolver");
        check(c != null, "loads");
        check(c.isInterface(), "is interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        Class<?>[] ifs = c.getInterfaces();
        check(ifs.length == 1, "1 direct superinterface(s)");
        check(ifs[0].getName().equals("javax.sql.RowSet"), "superinterface 0");
        check(c.getDeclaredMethods().length == 7, "declared method count 7");

        java.lang.reflect.Field f_UPDATE_ROW_CONFLICT = c.getField("UPDATE_ROW_CONFLICT");
        check(f_UPDATE_ROW_CONFLICT.getInt(null) == 0, "UPDATE_ROW_CONFLICT == 0");
        java.lang.reflect.Field f_DELETE_ROW_CONFLICT = c.getField("DELETE_ROW_CONFLICT");
        check(f_DELETE_ROW_CONFLICT.getInt(null) == 1, "DELETE_ROW_CONFLICT == 1");
        java.lang.reflect.Field f_INSERT_ROW_CONFLICT = c.getField("INSERT_ROW_CONFLICT");
        check(f_INSERT_ROW_CONFLICT.getInt(null) == 2, "INSERT_ROW_CONFLICT == 2");
        java.lang.reflect.Field f_NO_ROW_CONFLICT = c.getField("NO_ROW_CONFLICT");
        check(f_NO_ROW_CONFLICT.getInt(null) == 3, "NO_ROW_CONFLICT == 3");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // Reference implementation of SyncResolver is produced by a SyncProviderException.
        SyncResolver resolver = new SyncProviderException().getSyncResolver();
        check(resolver != null, "resolver non-null");
        check(resolver instanceof RowSet, "resolver is a RowSet (via SyncResolver extends RowSet)");

        // Fresh resolver has no conflict rows; status reports NO_ROW_CONFLICT.
        check(SyncResolver.UPDATE_ROW_CONFLICT == 0, "UPDATE_ROW_CONFLICT == 0");
        check(SyncResolver.DELETE_ROW_CONFLICT == 1, "DELETE_ROW_CONFLICT == 1");
        check(SyncResolver.INSERT_ROW_CONFLICT == 2, "INSERT_ROW_CONFLICT == 2");
        check(SyncResolver.NO_ROW_CONFLICT == 3, "NO_ROW_CONFLICT == 3");
        check(resolver.getStatus() == SyncResolver.NO_ROW_CONFLICT, "fresh resolver getStatus NO_ROW_CONFLICT");
        check(resolver.previousConflict() == false, "fresh resolver previousConflict false");
        check(resolver.nextConflict() == false, "fresh resolver nextConflict false");

        // Conflict value accessors surface SQLException on an empty resolver.
        checkThrows(SQLException.class, () -> resolver.getConflictValue(1), "getConflictValue(int) throws");
        checkThrows(SQLException.class, () -> resolver.getConflictValue("c"), "getConflictValue(String) throws");
        checkThrows(SQLException.class, () -> resolver.setResolvedValue(1, "v"), "setResolvedValue(int) throws");
        checkThrows(SQLException.class, () -> resolver.setResolvedValue("c", "v"), "setResolvedValue(String) throws");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
