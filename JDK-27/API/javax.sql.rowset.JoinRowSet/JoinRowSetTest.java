/*
 * @test
 * @summary javax.sql.rowset.JoinRowSet conformance: white-box structure + black-box behavior.
 * @run main JoinRowSetTest
 */

import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.JoinRowSet;
import javax.sql.rowset.RowSetFactory;
import javax.sql.rowset.RowSetMetaDataImpl;
import javax.sql.rowset.RowSetProvider;

public class JoinRowSetTest {
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

    static CachedRowSet makeCached(RowSetFactory f, String table, String idCol, String nameCol,
                                   int[][] ids, String[] names) throws SQLException {
        CachedRowSet crs = f.createCachedRowSet();
        RowSetMetaDataImpl md = new RowSetMetaDataImpl();
        md.setColumnCount(2);
        md.setColumnName(1, idCol);
        md.setColumnType(1, Types.INTEGER);
        md.setColumnName(2, nameCol);
        md.setColumnType(2, Types.VARCHAR);
        crs.setMetaData(md);
        crs.setTableName(table);
        for (int i = 0; i < ids.length; i++) {
            crs.moveToInsertRow();
            crs.updateInt(1, ids[i][0]);
            crs.updateString(2, names[i]);
            crs.insertRow();
        }
        crs.moveToCurrentRow();
        crs.beforeFirst();
        return crs;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.sql.rowset.JoinRowSet");
        check(c != null, "loads");
        check(c.isInterface(), "is interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        Class<?>[] ifs = c.getInterfaces();
        check(ifs.length == 1, "1 direct superinterface(s)");
        check(ifs[0].getName().equals("javax.sql.rowset.WebRowSet"), "superinterface 0");
        check(c.getDeclaredMethods().length == 16, "declared method count 16");
        java.lang.reflect.Field f_CROSS_JOIN = c.getField("CROSS_JOIN");
        check(f_CROSS_JOIN.getInt(null) == 0, "CROSS_JOIN == 0");
        java.lang.reflect.Field f_INNER_JOIN = c.getField("INNER_JOIN");
        check(f_INNER_JOIN.getInt(null) == 1, "INNER_JOIN == 1");
        java.lang.reflect.Field f_LEFT_OUTER_JOIN = c.getField("LEFT_OUTER_JOIN");
        check(f_LEFT_OUTER_JOIN.getInt(null) == 2, "LEFT_OUTER_JOIN == 2");
        java.lang.reflect.Field f_RIGHT_OUTER_JOIN = c.getField("RIGHT_OUTER_JOIN");
        check(f_RIGHT_OUTER_JOIN.getInt(null) == 3, "RIGHT_OUTER_JOIN == 3");
        java.lang.reflect.Field f_FULL_JOIN = c.getField("FULL_JOIN");
        check(f_FULL_JOIN.getInt(null) == 4, "FULL_JOIN == 4");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        RowSetFactory f = RowSetProvider.newFactory();
        JoinRowSet jrs = f.createJoinRowSet();
        check(jrs != null, "createJoinRowSet non-null");
        check(jrs instanceof JoinRowSet, "instanceof JoinRowSet");
        check(jrs instanceof javax.sql.rowset.WebRowSet, "instanceof WebRowSet");
        check(jrs instanceof javax.sql.rowset.CachedRowSet, "instanceof CachedRowSet");
        check(jrs.getClass().getName().equals("com.sun.rowset.JoinRowSetImpl"), "impl class name");

        // supported join types
        check(!jrs.supportsCrossJoin(), "supportsCrossJoin false");
        check(jrs.supportsInnerJoin(), "supportsInnerJoin true");
        check(!jrs.supportsLeftOuterJoin(), "supportsLeftOuterJoin false");
        check(!jrs.supportsRightOuterJoin(), "supportsRightOuterJoin false");
        check(!jrs.supportsFullJoin(), "supportsFullJoin false");
        checkThrows(SQLException.class, () -> jrs.setJoinType(JoinRowSet.CROSS_JOIN), "setJoinType(CROSS_JOIN) unsupported throws SQLException");
        jrs.setJoinType(JoinRowSet.INNER_JOIN);

        // build two rowsets sharing the match column name "ID"
        CachedRowSet a = makeCached(f, "A", "ID", "LEFT_NAME",
                new int[][]{{1}, {2}, {3}}, new String[]{"A1", "A2", "A3"});
        CachedRowSet b = makeCached(f, "B", "ID", "RIGHT_NAME",
                new int[][]{{2}, {3}, {4}}, new String[]{"B2", "B3", "B4"});

        jrs.addRowSet(a, "ID");
        jrs.addRowSet(b, "ID");

        check(jrs.getJoinType() == JoinRowSet.INNER_JOIN, "getJoinType INNER_JOIN after join");
        check(jrs.getRowSets().size() == 2, "getRowSets size 2");
        String[] names = jrs.getRowSetNames();
        check(names.length == 2 && "A".equals(names[0]) && "B".equals(names[1]), "getRowSetNames A,B");
        check(jrs.getWhereClause() != null && jrs.getWhereClause().toLowerCase(java.util.Locale.ROOT).contains("where"),
                "getWhereClause contains where");

        // inner join on ID matches rows 2 and 3
        check(jrs.size() == 2, "join result size 2");
        List<Object[]> rows = new ArrayList<>();
        jrs.beforeFirst();
        while (jrs.next()) {
            rows.add(new Object[]{ jrs.getInt(1), jrs.getString(2), jrs.getString(3) });
        }
        check(rows.size() == 2, "read 2 joined rows");
        rows.sort((x, y) -> ((Integer) x[0]).compareTo((Integer) y[0]));
        check(((Integer) rows.get(0)[0]) == 2 && "A2".equals(rows.get(0)[1]) && "B2".equals(rows.get(0)[2]), "joined row 2");
        check(((Integer) rows.get(1)[0]) == 3 && "A3".equals(rows.get(1)[1]) && "B3".equals(rows.get(1)[2]), "joined row 3");

        // metadata of the join result has 3 columns (ID merged once + two names)
        java.sql.ResultSetMetaData jmd = jrs.getMetaData();
        check(jmd.getColumnCount() == 3, "join metadata column count 3");
        check("ID".equals(jmd.getColumnName(1)), "join col 1 name ID");

        // toCachedRowSet carries the joined data
        CachedRowSet flat = jrs.toCachedRowSet();
        check(flat != null && flat.size() == 2, "toCachedRowSet size 2");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
