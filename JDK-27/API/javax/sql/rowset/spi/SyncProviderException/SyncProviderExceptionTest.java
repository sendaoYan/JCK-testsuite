/*
 * @test
 * @summary javax.sql.rowset.spi.SyncProviderException conformance: white-box structure + black-box behavior.
 * @run main SyncProviderExceptionTest
 */

import java.sql.SQLException;
import javax.sql.rowset.spi.SyncProviderException;
import javax.sql.rowset.spi.SyncResolver;

public class SyncProviderExceptionTest {
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
        Class<?> c = Class.forName("javax.sql.rowset.spi.SyncProviderException");
        check(!c.isInterface(), "is a class");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("java.sql.SQLException"), "extends SQLException");
        check(c.getDeclaredConstructors().length == 3, "declared constructor count 3");
        check(c.getDeclaredMethods().length == 2, "declared method count 2");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // no-arg and String constructors.
        SyncProviderException e0 = new SyncProviderException();
        check(e0.getMessage() == null, "no-arg message null");
        check(e0 instanceof SQLException, "no-arg instanceof SQLException");
        SyncResolver dflt = e0.getSyncResolver();
        check(dflt != null, "no-arg default resolver non-null");
        check(dflt instanceof SyncResolver, "default resolver is SyncResolver");

        SyncProviderException e1 = new SyncProviderException("m");
        check("m".equals(e1.getMessage()), "String ctor message");
        check(e1.getSyncResolver() != null, "String ctor lazily creates default resolver");

        // Default resolver behavior (fresh, no conflicts).
        check(dflt.getStatus() == SyncResolver.NO_ROW_CONFLICT, "default resolver status NO_ROW_CONFLICT");
        check(dflt.previousConflict() == false, "default resolver previousConflict false");
        check(dflt.nextConflict() == false, "default resolver nextConflict false");
        checkThrows(SQLException.class, () -> dflt.getConflictValue(1), "default resolver getConflictValue(int) throws");
        checkThrows(SQLException.class, () -> dflt.getConflictValue("c"), "default resolver getConflictValue(String) throws");
        checkThrows(SQLException.class, () -> dflt.setResolvedValue(1, "v"), "default resolver setResolvedValue(int) throws");
        checkThrows(SQLException.class, () -> dflt.setResolvedValue("c", "v"), "default resolver setResolvedValue(String) throws");

        // SyncResolver constructor and setter round-trip.
        SyncResolver r = new SyncProviderException().getSyncResolver();
        SyncProviderException e2 = new SyncProviderException(r);
        check(e2.getSyncResolver() == r, "SyncResolver ctor stores resolver");
        check(e2.getMessage() == null, "SyncResolver ctor message null");

        SyncResolver r2 = new SyncProviderException().getSyncResolver();
        SyncProviderException e3 = new SyncProviderException("x");
        e3.setSyncResolver(r2);
        check(e3.getSyncResolver() == r2, "setSyncResolver round-trip");

        // Null SyncResolver is rejected.
        checkThrows(IllegalArgumentException.class, () -> new SyncProviderException((SyncResolver) null), "SyncResolver(null) throws IAE");
        checkThrows(IllegalArgumentException.class, () -> new SyncProviderException("x").setSyncResolver(null), "setSyncResolver(null) throws IAE");

        // Cause chain inherited from SQLException.
        IllegalStateException cause = new IllegalStateException("root");
        SyncProviderException e4 = new SyncProviderException("with cause");
        check(e4.initCause(cause) == e4, "initCause returns this");
        check(e4.getCause() == cause, "getCause after initCause");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
