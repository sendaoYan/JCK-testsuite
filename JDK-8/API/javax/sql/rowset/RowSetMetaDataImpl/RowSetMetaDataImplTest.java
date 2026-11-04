/*
 * @test
 * @summary javax.sql.rowset.RowSetMetaDataImpl conformance: column count/name/nullable.
 * @run main RowSetMetaDataImplTest
 */


public class RowSetMetaDataImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        javax.sql.rowset.RowSetMetaDataImpl m = new javax.sql.rowset.RowSetMetaDataImpl();
        m.setColumnCount(2);
        check(m.getColumnCount() == 2, "getColumnCount");
        m.setColumnName(1, "id");
        m.setColumnType(1, java.sql.Types.INTEGER);
        m.setNullable(1, javax.sql.rowset.RowSetMetaDataImpl.columnNoNulls);
        check("id".equals(m.getColumnName(1)), "getColumnName");
        check(m.getColumnType(1) == java.sql.Types.INTEGER, "getColumnType");
        check(m.isNullable(1) == javax.sql.rowset.RowSetMetaDataImpl.columnNoNulls, "isNullable");
        check(m instanceof javax.sql.RowSetMetaData, "is RowSetMetaData");        System.out.println("PASS (" + passed + " assertions)");
    }
}
