/*
 * @test
 * @summary javax.sql.rowset.CachedRowSet conformance: white-box structure + black-box behavior.
 * @run main CachedRowSetTest
 */

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import javax.sql.RowSet;
import javax.sql.RowSetMetaData;
import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetFactory;
import javax.sql.rowset.RowSetMetaDataImpl;
import javax.sql.rowset.RowSetProvider;

public class CachedRowSetTest {
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

    static CachedRowSet fill(RowSetFactory f) throws SQLException {
        CachedRowSet crs = f.createCachedRowSet();
        RowSetMetaDataImpl md = new RowSetMetaDataImpl();
        md.setColumnCount(2);
        md.setColumnName(1, "ID");
        md.setColumnType(1, Types.INTEGER);
        md.setColumnName(2, "NAME");
        md.setColumnType(2, Types.VARCHAR);
        crs.setMetaData(md);
        crs.moveToInsertRow(); crs.updateInt(1, 1); crs.updateString(2, "Alice"); crs.insertRow();
        crs.moveToInsertRow(); crs.updateInt(1, 2); crs.updateString(2, "Bob"); crs.insertRow();
        crs.moveToInsertRow(); crs.updateInt(1, 3); crs.updateString(2, "Carol"); crs.insertRow();
        crs.moveToCurrentRow();
        crs.beforeFirst();
        return crs;
    }

    static List<Object[]> readRows(CachedRowSet crs) throws SQLException {
        List<Object[]> rows = new ArrayList<>();
        crs.beforeFirst();
        while (crs.next()) {
            rows.add(new Object[]{ crs.getInt(1), crs.getString(2) });
        }
        return rows;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.sql.rowset.CachedRowSet");
        check(c != null, "loads");
        check(c.isInterface(), "is interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        Class<?>[] ifs = c.getInterfaces();
        check(ifs.length == 2, "2 direct superinterface(s)");
        check(ifs[0].getName().equals("javax.sql.RowSet"), "superinterface 0");
        check(ifs[1].getName().equals("javax.sql.rowset.Joinable"), "superinterface 1");
        check(c.getDeclaredMethods().length == 41, "declared method count 41");
        java.lang.reflect.Field f_COMMIT_ON_ACCEPT_CHANGES = c.getField("COMMIT_ON_ACCEPT_CHANGES");
        check(f_COMMIT_ON_ACCEPT_CHANGES.getBoolean(null) == true, "COMMIT_ON_ACCEPT_CHANGES == true");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        RowSetFactory f = RowSetProvider.newFactory();
        CachedRowSet crs = f.createCachedRowSet();
        check(crs != null, "createCachedRowSet non-null");
        check(crs instanceof CachedRowSet, "instanceof CachedRowSet");
        check(crs instanceof javax.sql.rowset.Joinable, "instanceof Joinable");
        check(crs instanceof RowSet, "instanceof RowSet");
        check(crs instanceof ResultSet, "instanceof ResultSet");
        check(crs.getClass().getName().equals("com.sun.rowset.CachedRowSetImpl"), "impl class name");

        // metadata + multi-row populate in memory
        RowSetMetaDataImpl md = new RowSetMetaDataImpl();
        md.setColumnCount(2);
        md.setColumnName(1, "ID");
        md.setColumnType(1, Types.INTEGER);
        md.setColumnName(2, "NAME");
        md.setColumnType(2, Types.VARCHAR);
        crs.setMetaData(md);
        check(crs.size() == 0, "empty size before rows");
        crs.moveToInsertRow(); crs.updateInt(1, 1); crs.updateString(2, "Alice"); crs.insertRow();
        crs.moveToInsertRow(); crs.updateInt(1, 2); crs.updateString(2, "Bob"); crs.insertRow();
        crs.moveToInsertRow(); crs.updateInt(1, 3); crs.updateString(2, "Carol"); crs.insertRow();
        crs.moveToCurrentRow();
        check(crs.size() == 3, "size 3 after inserts");

        // iteration in insertion order
        List<Object[]> rows = readRows(crs);
        check(rows.size() == 3, "read 3 rows");
        check(((Integer) rows.get(0)[0]) == 1 && "Alice".equals(rows.get(0)[1]), "row 0");
        check(((Integer) rows.get(1)[0]) == 2 && "Bob".equals(rows.get(1)[1]), "row 1");
        check(((Integer) rows.get(2)[0]) == 3 && "Carol".equals(rows.get(2)[1]), "row 2");

        // metadata roundtrip
        java.sql.ResultSetMetaData m = crs.getMetaData();
        check(m.getColumnCount() == 2, "meta column count 2");
        check("ID".equals(m.getColumnName(1)), "meta col 1 name");
        check(m.getColumnType(1) == Types.INTEGER, "meta col 1 type");
        check("NAME".equals(m.getColumnName(2)), "meta col 2 name");
        check(m.getColumnType(2) == Types.VARCHAR, "meta col 2 type");

        // findColumn
        check(crs.findColumn("ID") == 1, "findColumn ID == 1");
        check(crs.findColumn("NAME") == 2, "findColumn NAME == 2");
        checkThrows(SQLException.class, () -> crs.findColumn("NOPE"), "findColumn unknown throws SQLException");

        // cursor movement
        crs.beforeFirst();
        check(crs.isBeforeFirst(), "isBeforeFirst true");
        check(!crs.isFirst(), "isFirst false while beforeFirst");
        check(crs.first(), "first");
        check(crs.isFirst(), "isFirst true after first");
        check(crs.last(), "last");
        check(crs.isLast(), "isLast true after last");
        check(crs.absolute(2), "absolute(2)");
        check(crs.getRow() == 2, "getRow == 2");
        check(crs.relative(-1), "relative(-1)");
        check(crs.getRow() == 1, "getRow == 1 after relative(-1)");
        check(!crs.absolute(999), "absolute(999) beyond last returns false");
        check(crs.isAfterLast(), "isAfterLast true after absolute(999)");
        check(crs.previous(), "previous");
        check(crs.isLast(), "isLast true after previous from afterLast");

        // read-only roundtrip (inherited from BaseRowSet)
        crs.setReadOnly(false);
        check(!crs.isReadOnly(), "setReadOnly(false)");
        crs.setReadOnly(true);
        check(crs.isReadOnly(), "setReadOnly(true)/isReadOnly");

        // toCollection
        crs.beforeFirst();
        List<Object> col1 = new ArrayList<>(crs.toCollection(1));
        check(col1.size() == 3, "toCollection(1) size 3");
        check(((Integer) col1.get(0)) == 1 && ((Integer) col1.get(1)) == 2 && ((Integer) col1.get(2)) == 3,
                "toCollection(1) values");
        List<Object> colName = new ArrayList<>(crs.toCollection("NAME"));
        check(colName.size() == 3, "toCollection(NAME) size 3");
        check("Alice".equals(colName.get(0)) && "Bob".equals(colName.get(1)) && "Carol".equals(colName.get(2)),
                "toCollection(NAME) values");

        // sync provider present
        check(crs.getSyncProvider() != null, "getSyncProvider non-null");

        // table name / key columns / page size
        check(crs.getTableName() == null, "getTableName default null");
        crs.setTableName("T");
        check("T".equals(crs.getTableName()), "setTableName/getTableName");
        checkThrows(SQLException.class, () -> crs.setTableName(null), "setTableName(null) throws SQLException");
        crs.setKeyColumns(new int[]{1});
        int[] keys = crs.getKeyColumns();
        check(keys != null && keys.length == 1 && keys[0] == 1, "setKeyColumns/getKeyColumns");
        checkThrows(SQLException.class, () -> crs.setKeyColumns(new int[]{5}), "setKeyColumns invalid col throws SQLException");
        check(crs.getPageSize() == 0, "getPageSize default 0");
        crs.setPageSize(2);
        check(crs.getPageSize() == 2, "setPageSize/getPageSize");
        checkThrows(SQLException.class, () -> crs.setPageSize(-1), "setPageSize negative throws SQLException");

        // createCopy carries the same data
        CachedRowSet copy = crs.createCopy();
        check(copy != null, "createCopy non-null");
        check(copy.size() == 3, "createCopy size 3");
        List<Object[]> copyRows = readRows(copy);
        check(copyRows.size() == 3 && ((Integer) copyRows.get(0)[0]) == 1
                && "Carol".equals(copyRows.get(2)[1]), "createCopy data preserved");

        // createCopySchema keeps metadata but no rows
        CachedRowSet schemaCopy = crs.createCopySchema();
        check(schemaCopy.size() == 0, "createCopySchema size 0");
        check(schemaCopy.getMetaData().getColumnCount() == 2, "createCopySchema metadata preserved");

        // createCopyNoConstraints keeps rows
        CachedRowSet noConst = crs.createCopyNoConstraints();
        check(noConst.size() == 3, "createCopyNoConstraints size 3");

        // createShared returns a RowSet view
        RowSet shared = crs.createShared();
        check(shared != null && shared instanceof ResultSet, "createShared non-null ResultSet");

        // undoInsert removes the just-inserted row (inserted before the current row)
        CachedRowSet crs2 = fill(f);
        check(crs2.size() == 3, "fresh size 3");
        crs2.moveToInsertRow(); crs2.updateInt(1, 99); crs2.updateString(2, "Temp"); crs2.insertRow();
        crs2.moveToCurrentRow();
        check(crs2.size() == 4, "size 4 after extra insert");
        crs2.first();
        check(crs2.getInt(1) == 99, "first row is inserted row");
        crs2.undoInsert();
        check(crs2.size() == 3, "undoInsert removes inserted row");

        // undoUpdate on the insert row throws SQLException
        CachedRowSet crsB = fill(f);
        crsB.moveToInsertRow();
        crsB.updateInt(1, 77); crsB.updateString(2, "X");
        checkThrows(SQLException.class, () -> crsB.undoUpdate(), "undoUpdate on insert row throws SQLException");

        // undoUpdate on an inserted row reverts the insert
        CachedRowSet crs3 = fill(f);
        crs3.moveToInsertRow(); crs3.updateInt(1, 77); crs3.updateString(2, "X"); crs3.insertRow();
        crs3.moveToCurrentRow();
        check(crs3.size() == 4, "size 4 before undoUpdate");
        crs3.first();
        crs3.undoUpdate();
        check(crs3.size() == 3, "undoUpdate reverts inserted row");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
