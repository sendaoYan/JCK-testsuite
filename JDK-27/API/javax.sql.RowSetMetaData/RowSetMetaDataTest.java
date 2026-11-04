/*
 * @test
 * @summary javax.sql.RowSetMetaData conformance: white-box structure + black-box behavior.
 * @run main RowSetMetaDataTest
 */
public class RowSetMetaDataTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    interface Throwing { void run() throws Throwable; }
    static void checkThrows(Class<? extends Throwable> t, String label, Throwing r) {
        try { r.run(); } catch (Throwable x) { check(t.isInstance(x), label + " throws " + t.getSimpleName()); return; }
        throw new AssertionError(label + ": expected " + t.getSimpleName());
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.sql.RowSetMetaData");
        check(c != null, "loads");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(!c.isAnnotation(), "not annotation");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract");
        Class<?>[] ifs = c.getInterfaces();
        check(ifs.length == 1, "1 direct superinterface(s)");
        check(ifs[0].getName().equals("java.sql.ResultSetMetaData"), "superinterface 0");
        check(c.getDeclaredMethods().length == 17, "declared method count 17");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        check(javax.sql.RowSetMetaData.class.isAssignableFrom(javax.sql.rowset.RowSetMetaDataImpl.class), "RowSetMetaDataImpl implements RowSetMetaData");

        // Use the JDK concrete impl, but call every setter through the interface type.
        javax.sql.RowSetMetaData rsm = new javax.sql.rowset.RowSetMetaDataImpl();
        rsm.setColumnCount(2);

        rsm.setAutoIncrement(1, true);
        rsm.setCaseSensitive(1, true);
        rsm.setSearchable(1, true);
        rsm.setCurrency(1, false);
        rsm.setNullable(1, java.sql.ResultSetMetaData.columnNullable);
        rsm.setSigned(1, true);
        rsm.setColumnDisplaySize(1, 20);
        rsm.setColumnLabel(1, "LBL");
        rsm.setColumnName(1, "NAME");
        rsm.setSchemaName(1, "SCH");
        rsm.setPrecision(1, 10);
        rsm.setScale(1, 2);
        rsm.setTableName(1, "TBL");
        rsm.setCatalogName(1, "CAT");
        rsm.setColumnType(1, java.sql.Types.VARCHAR);
        rsm.setColumnTypeName(1, "VARCHAR");

        // Round-trip through the inherited getters to observe real behavior.
        check(rsm.getColumnCount() == 2, "setColumnCount/getColumnCount");
        check(rsm.isAutoIncrement(1), "setAutoIncrement/isAutoIncrement");
        check(rsm.isCaseSensitive(1), "setCaseSensitive/isCaseSensitive");
        check(rsm.isSearchable(1), "setSearchable/isSearchable");
        check(!rsm.isCurrency(1), "setCurrency/isCurrency");
        check(rsm.isNullable(1) == java.sql.ResultSetMetaData.columnNullable, "setNullable/isNullable");
        check(rsm.isSigned(1), "setSigned/isSigned");
        check(rsm.getColumnDisplaySize(1) == 20, "setColumnDisplaySize/getColumnDisplaySize");
        check("LBL".equals(rsm.getColumnLabel(1)), "setColumnLabel/getColumnLabel");
        check("NAME".equals(rsm.getColumnName(1)), "setColumnName/getColumnName");
        check("SCH".equals(rsm.getSchemaName(1)), "setSchemaName/getSchemaName");
        check(rsm.getPrecision(1) == 10, "setPrecision/getPrecision");
        check(rsm.getScale(1) == 2, "setScale/getScale");
        check("TBL".equals(rsm.getTableName(1)), "setTableName/getTableName");
        check("CAT".equals(rsm.getCatalogName(1)), "setCatalogName/getCatalogName");
        check(rsm.getColumnType(1) == java.sql.Types.VARCHAR, "setColumnType/getColumnType");
        check("VARCHAR".equals(rsm.getColumnTypeName(1)), "setColumnTypeName/getColumnTypeName");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
