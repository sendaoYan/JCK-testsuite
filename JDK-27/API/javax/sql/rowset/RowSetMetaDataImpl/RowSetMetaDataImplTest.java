/*
 * @test
 * @summary javax.sql.rowset.RowSetMetaDataImpl conformance: white-box structure + black-box behavior.
 * @run main RowSetMetaDataImplTest
 */

import java.sql.SQLException;
import java.sql.Types;
import javax.sql.RowSetMetaData;
import javax.sql.rowset.RowSetMetaDataImpl;

public class RowSetMetaDataImplTest {
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
        Class<?> c = Class.forName("javax.sql.rowset.RowSetMetaDataImpl");
        check(c != null, "loads");
        check(!c.isInterface(), "is concrete class");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass() == Object.class, "superclass Object");
        Class<?>[] ifs = c.getInterfaces();
        check(ifs.length == 2, "implements 2 interfaces");
        check(ifs[0].getName().equals("javax.sql.RowSetMetaData"), "implements RowSetMetaData");
        check(ifs[1].getName().equals("java.io.Serializable"), "implements Serializable");
        check(c.getDeclaredConstructors().length == 1, "one declared constructor");
        check(c.getDeclaredConstructors()[0].getParameterCount() == 0, "no-arg constructor");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        RowSetMetaDataImpl md = new RowSetMetaDataImpl();

        // column count
        md.setColumnCount(2);
        check(md.getColumnCount() == 2, "setColumnCount/getColumnCount");
        checkThrows(SQLException.class, () -> md.setColumnCount(0), "setColumnCount(0) throws SQLException");
        checkThrows(SQLException.class, () -> md.setColumnCount(-1), "setColumnCount(-1) throws SQLException");

        // name / type / type name / class name
        md.setColumnName(1, "A");
        check("A".equals(md.getColumnName(1)), "setColumnName/getColumnName");
        md.setColumnType(1, Types.INTEGER);
        check(md.getColumnType(1) == Types.INTEGER, "setColumnType/getColumnType");
        md.setColumnTypeName(1, "INTEGER");
        check("INTEGER".equals(md.getColumnTypeName(1)), "setColumnTypeName/getColumnTypeName");
        check("java.lang.Integer".equals(md.getColumnClassName(1)), "getColumnClassName INTEGER");
        md.setColumnType(2, Types.VARCHAR);
        md.setColumnTypeName(2, "VARCHAR");
        check("java.lang.String".equals(md.getColumnClassName(2)), "getColumnClassName VARCHAR default");

        // out-of-range access
        checkThrows(SQLException.class, () -> md.getColumnName(0), "getColumnName(0) throws SQLException");
        checkThrows(SQLException.class, () -> md.getColumnName(3), "getColumnName(3) throws SQLException");
        checkThrows(SQLException.class, () -> md.setColumnType(0, Types.INTEGER), "setColumnType(0) throws SQLException");

        // boolean metadata roundtrips
        md.setAutoIncrement(1, true);
        check(md.isAutoIncrement(1), "setAutoIncrement/isAutoIncrement");
        md.setCaseSensitive(1, true);
        check(md.isCaseSensitive(1), "setCaseSensitive/isCaseSensitive");
        md.setSearchable(1, false);
        check(!md.isSearchable(1), "setSearchable/isSearchable");
        md.setCurrency(1, true);
        check(md.isCurrency(1), "setCurrency/isCurrency");
        md.setSigned(1, false);
        check(!md.isSigned(1), "setSigned/isSigned");

        // nullable
        md.setNullable(1, RowSetMetaData.columnNullable);
        check(md.isNullable(1) == RowSetMetaData.columnNullable, "setNullable/isNullable");
        md.setNullable(1, RowSetMetaData.columnNoNulls);
        check(md.isNullable(1) == RowSetMetaData.columnNoNulls, "setNullable columnNoNulls");

        // string/int metadata roundtrips
        md.setColumnDisplaySize(1, 20);
        check(md.getColumnDisplaySize(1) == 20, "setColumnDisplaySize/getColumnDisplaySize");
        md.setColumnLabel(1, "Lbl");
        check("Lbl".equals(md.getColumnLabel(1)), "setColumnLabel/getColumnLabel");
        md.setSchemaName(1, "S");
        check("S".equals(md.getSchemaName(1)), "setSchemaName/getSchemaName");
        md.setPrecision(1, 10);
        check(md.getPrecision(1) == 10, "setPrecision/getPrecision");
        md.setScale(1, 3);
        check(md.getScale(1) == 3, "setScale/getScale");
        md.setTableName(1, "T");
        check("T".equals(md.getTableName(1)), "setTableName/getTableName");
        md.setCatalogName(1, "C");
        check("C".equals(md.getCatalogName(1)), "setCatalogName/getCatalogName");

        // read-only / definitely-writable defaults
        check(!md.isReadOnly(1), "isReadOnly default false");
        check(md.isDefinitelyWritable(1), "isDefinitelyWritable true");

        // wrapper
        check(md.isWrapperFor(RowSetMetaData.class), "isWrapperFor RowSetMetaData");
        check(md.unwrap(RowSetMetaData.class) == md, "unwrap self");
        check(!md.isWrapperFor(String.class), "isWrapperFor String false");
        checkThrows(SQLException.class, () -> md.unwrap(String.class), "unwrap(String) throws SQLException");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
