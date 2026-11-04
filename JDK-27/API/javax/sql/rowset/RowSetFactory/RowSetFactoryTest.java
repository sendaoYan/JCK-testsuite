/*
 * @test
 * @summary javax.sql.rowset.RowSetFactory conformance: white-box structure + black-box behavior.
 * @run main RowSetFactoryTest
 */

import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.FilteredRowSet;
import javax.sql.rowset.JdbcRowSet;
import javax.sql.rowset.JoinRowSet;
import javax.sql.rowset.RowSetFactory;
import javax.sql.rowset.RowSetProvider;
import javax.sql.rowset.WebRowSet;

public class RowSetFactoryTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.sql.rowset.RowSetFactory");
        check(c != null, "loads");
        check(c.isInterface(), "is interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        Class<?>[] ifs = c.getInterfaces();
        check(ifs.length == 0, "0 direct superinterface(s)");
        check(c.getDeclaredMethods().length == 5, "declared method count 5");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        RowSetFactory f = RowSetProvider.newFactory();
        check(f != null, "newFactory non-null");
        check(f instanceof RowSetFactory, "instanceof RowSetFactory");
        check(f.getClass().getName().equals("com.sun.rowset.RowSetFactoryImpl"), "impl class name");

        CachedRowSet crs = f.createCachedRowSet();
        check(crs instanceof CachedRowSet, "createCachedRowSet instanceof CachedRowSet");
        check(crs.getClass().getName().equals("com.sun.rowset.CachedRowSetImpl"), "CachedRowSet impl");

        FilteredRowSet frs = f.createFilteredRowSet();
        check(frs instanceof FilteredRowSet, "createFilteredRowSet instanceof FilteredRowSet");
        check(frs.getClass().getName().equals("com.sun.rowset.FilteredRowSetImpl"), "FilteredRowSet impl");

        JdbcRowSet jrs = f.createJdbcRowSet();
        check(jrs instanceof JdbcRowSet, "createJdbcRowSet instanceof JdbcRowSet");
        check(jrs.getClass().getName().equals("com.sun.rowset.JdbcRowSetImpl"), "JdbcRowSet impl");

        JoinRowSet join = f.createJoinRowSet();
        check(join instanceof JoinRowSet, "createJoinRowSet instanceof JoinRowSet");
        check(join.getClass().getName().equals("com.sun.rowset.JoinRowSetImpl"), "JoinRowSet impl");

        WebRowSet wrs = f.createWebRowSet();
        check(wrs instanceof WebRowSet, "createWebRowSet instanceof WebRowSet");
        check(wrs.getClass().getName().equals("com.sun.rowset.WebRowSetImpl"), "WebRowSet impl");

        // Each created instance is distinct.
        check(f.createCachedRowSet() != f.createCachedRowSet(), "distinct CachedRowSet instances");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
