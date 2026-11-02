/*
 * @test
 * @summary javax.sql.rowset.RowSetProvider conformance: default and named RowSetFactory creation
 * @run main RowSetProviderTest
 */

public class RowSetProviderTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        javax.sql.rowset.RowSetFactory f = javax.sql.rowset.RowSetProvider.newFactory();
        check(f != null, "newFactory non-null");
        check(f.createCachedRowSet() != null, "createCachedRowSet");
        check(f.createFilteredRowSet() != null, "createFilteredRowSet");
        check(f.createJdbcRowSet() != null, "createJdbcRowSet");
        check(f.createJoinRowSet() != null, "createJoinRowSet");
        check(f.createWebRowSet() != null, "createWebRowSet");
        javax.sql.rowset.RowSetFactory f2 = javax.sql.rowset.RowSetProvider.newFactory("com.sun.rowset.RowSetFactoryImpl", null);
        check(f2 != null, "newFactory with class name");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
