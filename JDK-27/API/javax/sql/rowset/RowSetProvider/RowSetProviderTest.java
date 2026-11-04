/*
 * @test
 * @summary javax.sql.rowset.RowSetProvider conformance: white-box structure + black-box behavior.
 * @run main RowSetProviderTest
 */

import java.sql.SQLException;
import javax.sql.rowset.RowSetFactory;
import javax.sql.rowset.RowSetProvider;

public class RowSetProviderTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    interface ThrowableRunnable { void run() throws Exception; }
    static void checkThrows(Class<? extends Throwable> expected, ThrowableRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            throw new AssertionError(msg + " threw " + t.getClass().getName() + ": " + t, t);
        }
        throw new AssertionError(msg + " did not throw " + expected.getName());
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.sql.rowset.RowSetProvider");
        check(c != null, "loads");
        check(!c.isInterface(), "is concrete class");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass() == Object.class, "superclass Object");
        check(c.getDeclaredConstructors().length == 1, "one declared constructor");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        RowSetFactory f = RowSetProvider.newFactory();
        check(f != null, "newFactory non-null");
        check(f instanceof RowSetFactory, "newFactory instanceof RowSetFactory");
        check(f.getClass().getName().equals("com.sun.rowset.RowSetFactoryImpl"), "newFactory impl class");

        check(f.createCachedRowSet() != null, "createCachedRowSet");
        check(f.createFilteredRowSet() != null, "createFilteredRowSet");
        check(f.createJdbcRowSet() != null, "createJdbcRowSet");
        check(f.createJoinRowSet() != null, "createJoinRowSet");
        check(f.createWebRowSet() != null, "createWebRowSet");

        RowSetFactory f2 = RowSetProvider.newFactory("com.sun.rowset.RowSetFactoryImpl", null);
        check(f2 != null, "newFactory(name, null) non-null");
        check(f2.getClass().getName().equals("com.sun.rowset.RowSetFactoryImpl"), "named factory impl class");

        RowSetFactory f3 = RowSetProvider.newFactory("com.sun.rowset.RowSetFactoryImpl",
                RowSetProvider.class.getClassLoader());
        check(f3 != null, "newFactory(name, classLoader) non-null");

        checkThrows(SQLException.class, () -> RowSetProvider.newFactory("com.example.DoesNotExist", null),
                "newFactory invalid name throws SQLException");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
