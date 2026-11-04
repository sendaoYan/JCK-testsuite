/*
 * @test
 * @summary javax.sql.rowset.FilteredRowSet conformance: white-box structure + black-box behavior.
 * @run main FilteredRowSetTest
 */

import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import javax.sql.RowSet;
import javax.sql.rowset.FilteredRowSet;
import javax.sql.rowset.Predicate;
import javax.sql.rowset.RowSetFactory;
import javax.sql.rowset.RowSetMetaDataImpl;
import javax.sql.rowset.RowSetProvider;

public class FilteredRowSetTest {
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

    static FilteredRowSet fill(RowSetFactory f) throws SQLException {
        FilteredRowSet frs = f.createFilteredRowSet();
        RowSetMetaDataImpl md = new RowSetMetaDataImpl();
        md.setColumnCount(2);
        md.setColumnName(1, "ID");
        md.setColumnType(1, Types.INTEGER);
        md.setColumnName(2, "NAME");
        md.setColumnType(2, Types.VARCHAR);
        frs.setMetaData(md);
        frs.moveToInsertRow(); frs.updateInt(1, 1); frs.updateString(2, "Alice"); frs.insertRow();
        frs.moveToInsertRow(); frs.updateInt(1, 2); frs.updateString(2, "Bob"); frs.insertRow();
        frs.moveToInsertRow(); frs.updateInt(1, 3); frs.updateString(2, "Carol"); frs.insertRow();
        frs.moveToCurrentRow();
        frs.beforeFirst();
        return frs;
    }

    static List<Integer> readIds(FilteredRowSet frs) throws SQLException {
        List<Integer> ids = new ArrayList<>();
        frs.beforeFirst();
        while (frs.next()) {
            ids.add(frs.getInt(1));
        }
        return ids;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.sql.rowset.FilteredRowSet");
        check(c != null, "loads");
        check(c.isInterface(), "is interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        Class<?>[] ifs = c.getInterfaces();
        check(ifs.length == 1, "1 direct superinterface(s)");
        check(ifs[0].getName().equals("javax.sql.rowset.WebRowSet"), "superinterface 0");
        check(c.getDeclaredMethods().length == 2, "declared method count 2");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        RowSetFactory f = RowSetProvider.newFactory();
        FilteredRowSet frs = f.createFilteredRowSet();
        check(frs != null, "createFilteredRowSet non-null");
        check(frs instanceof FilteredRowSet, "instanceof FilteredRowSet");
        check(frs instanceof javax.sql.rowset.WebRowSet, "instanceof WebRowSet");
        check(frs instanceof javax.sql.rowset.CachedRowSet, "instanceof CachedRowSet");
        check(frs.getClass().getName().equals("com.sun.rowset.FilteredRowSetImpl"), "impl class name");

        // getFilter defaults to null, setFilter(null) resets
        check(frs.getFilter() == null, "getFilter default null");
        frs.setFilter(null);
        check(frs.getFilter() == null, "setFilter(null) -> null");

        frs = fill(f);
        check(frs.size() == 3, "size 3");
        check(readIds(frs).equals(java.util.Arrays.asList(1, 2, 3)), "unfiltered ids 1,2,3");

        // a predicate keeping only rows with ID >= 2
        Predicate keepLarge = new Predicate() {
            public boolean evaluate(RowSet rs) {
                try { return rs.getInt(1) >= 2; } catch (SQLException e) { return false; }
            }
            public boolean evaluate(Object value, int column) {
                return column == 1 && value instanceof Integer && ((Integer) value) >= 2;
            }
            public boolean evaluate(Object value, String columnName) {
                return "ID".equalsIgnoreCase(columnName) && value instanceof Integer && ((Integer) value) >= 2;
            }
        };
        frs.setFilter(keepLarge);
        check(frs.getFilter() == keepLarge, "getFilter returns same predicate");
        check(readIds(frs).equals(java.util.Arrays.asList(2, 3)), "filtered ids 2,3");

        // a predicate that filters by name prefix via evaluate(RowSet)
        Predicate nameStartsWithB = new Predicate() {
            public boolean evaluate(RowSet rs) {
                try { return rs.getString(2).startsWith("B"); } catch (SQLException e) { return false; }
            }
            public boolean evaluate(Object value, int column) {
                return column == 2 && value instanceof String && ((String) value).startsWith("B");
            }
            public boolean evaluate(Object value, String columnName) {
                return "NAME".equalsIgnoreCase(columnName) && value instanceof String && ((String) value).startsWith("B");
            }
        };
        frs.setFilter(nameStartsWithB);
        check(readIds(frs).equals(java.util.Arrays.asList(2)), "filtered by NAME prefix B");

        // clearing the filter restores all rows
        frs.setFilter(null);
        check(frs.getFilter() == null, "filter cleared");
        check(readIds(frs).equals(java.util.Arrays.asList(1, 2, 3)), "all rows after clear");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
