/*
 * @test
 * @summary javax.sql.rowset.serial.SerialArray conformance: baseTypeName/getArray.
 * @run main SerialArrayTest
 */


public class SerialArrayTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static java.sql.Array mkArray() {
        return new java.sql.Array() {
            public String getBaseTypeName() { return "VARCHAR"; }
            public int getBaseType() { return java.sql.Types.VARCHAR; }
            public Object getArray() { return new Object[]{"a", "b"}; }
            public Object getArray(java.util.Map<String, Class<?>> m) { return getArray(); }
            public Object getArray(long i, int c) { return getArray(); }
            public Object getArray(long i, int c, java.util.Map<String, Class<?>> m) { return getArray(); }
            public java.sql.ResultSet getResultSet() { return null; }
            public java.sql.ResultSet getResultSet(java.util.Map<String, Class<?>> m) { return null; }
            public java.sql.ResultSet getResultSet(long i, int c) { return null; }
            public java.sql.ResultSet getResultSet(long i, int c, java.util.Map<String, Class<?>> m) { return null; }
            public void free() {}
        };
    }
    public static void main(String[] args) throws Exception {
        javax.sql.rowset.serial.SerialArray sa = new javax.sql.rowset.serial.SerialArray(mkArray());
        check("VARCHAR".equals(sa.getBaseTypeName()), "getBaseTypeName");
        Object[] a = (Object[]) sa.getArray();
        check(a.length == 2 && "a".equals(a[0]), "getArray");
        check(sa.getBaseType() == java.sql.Types.VARCHAR, "getBaseType");        System.out.println("PASS (" + passed + " assertions)");
    }
}
