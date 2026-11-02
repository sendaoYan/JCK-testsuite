/*
 * @test
 * @summary javax.sql.rowset.RowSetMetaDataImpl conformance: column metadata setters and getters
 * @run main RowSetMetaDataImplTest
 */

public class RowSetMetaDataImplTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        javax.sql.rowset.RowSetMetaDataImpl md = new javax.sql.rowset.RowSetMetaDataImpl();
        md.setColumnCount(2);
        check(md.getColumnCount() == 2, "column count");
        md.setColumnName(1, "A");
        check("A".equals(md.getColumnName(1)), "column name");
        md.setColumnType(1, java.sql.Types.INTEGER);
        check(md.getColumnType(1) == java.sql.Types.INTEGER, "column type");
        md.setColumnTypeName(1, "INTEGER");
        check("INTEGER".equals(md.getColumnTypeName(1)), "column type name");
        check("java.lang.Integer".equals(md.getColumnClassName(1)), "column class name");
        md.setNullable(1, javax.sql.RowSetMetaData.columnNullable);
        check(md.isNullable(1) == javax.sql.RowSetMetaData.columnNullable, "nullable");
        md.setPrecision(1, 10);
        check(md.getPrecision(1) == 10, "precision");
        check(md.isWrapperFor(javax.sql.RowSetMetaData.class), "isWrapperFor RowSetMetaData");
        check(md.unwrap(javax.sql.RowSetMetaData.class) == md, "unwrap self");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
