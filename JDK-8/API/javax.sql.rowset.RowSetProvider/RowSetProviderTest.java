/*
 * @test
 * @summary javax.sql.rowset.RowSetProvider conformance: newFactory.
 * @run main RowSetProviderTest
 */


public class RowSetProviderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        javax.sql.rowset.RowSetFactory f = javax.sql.rowset.RowSetProvider.newFactory();
        check(f != null, "newFactory non-null");
        check(f.createCachedRowSet() != null, "createCachedRowSet");
        check(f.createJdbcRowSet() != null, "createJdbcRowSet");        System.out.println("PASS (" + passed + " assertions)");
    }
}
