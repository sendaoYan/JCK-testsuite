/*
 * @test
 * @summary javax.sql.rowset.Joinable conformance: white-box structure + black-box behavior.
 * @run main JoinableTest
 */

import java.sql.SQLException;
import java.util.Arrays;
import javax.sql.rowset.Joinable;
import javax.sql.rowset.RowSetFactory;
import javax.sql.rowset.RowSetProvider;

public class JoinableTest {
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
        Class<?> c = Class.forName("javax.sql.rowset.Joinable");
        check(c != null, "loads");
        check(c.isInterface(), "is interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        Class<?>[] ifs = c.getInterfaces();
        check(ifs.length == 0, "0 direct superinterface(s)");
        check(c.getDeclaredMethods().length == 10, "declared method count 10");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // Anonymous implementation roundtrip proves the contract is implementable.
        Joinable simple = new Joinable() {
            private int[] idx = {-1};
            private String[] names = {null};
            public void setMatchColumn(int columnIdx) { idx = new int[]{columnIdx}; }
            public void setMatchColumn(int[] columnIdxes) { idx = columnIdxes.clone(); }
            public void setMatchColumn(String columnName) { names = new String[]{columnName}; }
            public void setMatchColumn(String[] columnNames) { names = columnNames.clone(); }
            public int[] getMatchColumnIndexes() { return idx.clone(); }
            public String[] getMatchColumnNames() { return names.clone(); }
            public void unsetMatchColumn(int columnIdx) { idx = new int[]{-1}; }
            public void unsetMatchColumn(int[] columnIdxes) { idx = new int[]{-1}; }
            public void unsetMatchColumn(String columnName) { names = new String[]{null}; }
            public void unsetMatchColumn(String[] columnNames) { names = new String[]{null}; }
        };
        simple.setMatchColumn(2);
        check(simple.getMatchColumnIndexes()[0] == 2, "anonymous set/getMatchColumn(int)");
        simple.setMatchColumn("ID");
        check("ID".equals(simple.getMatchColumnNames()[0]), "anonymous set/getMatchColumn(String)");
        simple.setMatchColumn(new int[]{1, 2});
        check(Arrays.equals(simple.getMatchColumnIndexes(), new int[]{1, 2}), "anonymous set/getMatchColumn(int[])");
        simple.unsetMatchColumn(1);
        check(simple.getMatchColumnIndexes()[0] == -1, "anonymous unsetMatchColumn(int)");

        // Real Joinable implementation (CachedRowSetImpl) match-column contract.
        RowSetFactory f = RowSetProvider.newFactory();
        Joinable real = (Joinable) f.createCachedRowSet();
        check(real instanceof javax.sql.RowSet, "Joinable impl is also a RowSet");
        // Not set yet -> getters throw.
        checkThrows(SQLException.class, real::getMatchColumnIndexes, "getMatchColumnIndexes unset throws SQLException");
        checkThrows(SQLException.class, real::getMatchColumnNames, "getMatchColumnNames unset throws SQLException");

        // Use fresh instances for each setter to avoid the impl's additive match-column storage.
        Joinable idx = (Joinable) f.createCachedRowSet();
        idx.setMatchColumn(2);
        check(idx.getMatchColumnIndexes()[0] == 2, "real set/getMatchColumn(int)");

        Joinable idxArr = (Joinable) f.createCachedRowSet();
        idxArr.setMatchColumn(new int[]{1, 2});
        check(idxArr.getMatchColumnIndexes()[0] == 1 && idxArr.getMatchColumnIndexes()[1] == 2,
                "real set/getMatchColumn(int[])");

        Joinable nm = (Joinable) f.createCachedRowSet();
        nm.setMatchColumn("ID");
        check("ID".equals(nm.getMatchColumnNames()[0]), "real set/getMatchColumn(String)");

        Joinable nmArr = (Joinable) f.createCachedRowSet();
        nmArr.setMatchColumn(new String[]{"ID", "NAME"});
        check("ID".equals(nmArr.getMatchColumnNames()[0]) && "NAME".equals(nmArr.getMatchColumnNames()[1]),
                "real set/getMatchColumn(String[])");

        checkThrows(SQLException.class, () -> idx.setMatchColumn(-1), "setMatchColumn(-1) throws SQLException");
        checkThrows(SQLException.class, () -> idx.setMatchColumn(""), "setMatchColumn(empty) throws SQLException");
        checkThrows(SQLException.class, () -> idx.setMatchColumn((String) null), "setMatchColumn(null) throws SQLException");

        Joinable unset = (Joinable) f.createCachedRowSet();
        unset.setMatchColumn(1);
        unset.unsetMatchColumn(1);
        checkThrows(SQLException.class, unset::getMatchColumnIndexes, "unsetMatchColumn -> getMatchColumnIndexes throws again");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
