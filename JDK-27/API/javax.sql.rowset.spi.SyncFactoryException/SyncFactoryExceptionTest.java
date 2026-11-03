/*
 * @test
 * @summary javax.sql.rowset.spi.SyncFactoryException conformance: white-box structure + black-box behavior.
 * @run main SyncFactoryExceptionTest
 */

import java.sql.SQLException;
import javax.sql.rowset.spi.SyncFactoryException;

public class SyncFactoryExceptionTest {
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
        Class<?> c = Class.forName("javax.sql.rowset.spi.SyncFactoryException");
        check(!c.isInterface(), "is a class");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("java.sql.SQLException"), "extends SQLException");
        check(c.getDeclaredConstructors().length == 2, "declared constructor count 2");
        check(java.sql.SQLException.class.isAssignableFrom(c), "assignable to SQLException");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        SyncFactoryException e0 = new SyncFactoryException();
        check(e0.getMessage() == null, "no-arg message null");
        check(e0.getSQLState() == null, "no-arg SQLState null");
        check(e0.getErrorCode() == 0, "no-arg errorCode 0");
        check(e0 instanceof SQLException, "extends SQLException");

        SyncFactoryException e2 = new SyncFactoryException("m");
        check("m".equals(e2.getMessage()), "String ctor message");
        check(e2 instanceof SQLException, "String ctor instanceof SQLException");

        // Cause chain inherited from SQLException.
        IllegalStateException cause = new IllegalStateException("root");
        SyncFactoryException e3 = new SyncFactoryException("with cause");
        check(e3.initCause(cause) == e3, "initCause returns this");
        check(e3.getCause() == cause, "getCause after initCause");

        // SQLException next-exception chain.
        SyncFactoryException next = new SyncFactoryException("next");
        e3.setNextException(next);
        check(e3.getNextException() == next, "getNextException after setNextException");

        // Usable in a catch of SQLException.
        Throwable t = caught(() -> { throw new SyncFactoryException("thrown"); });
        check(t != null && t instanceof SyncFactoryException && t instanceof SQLException, "thrown and caught as SyncFactoryException/SQLException");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
