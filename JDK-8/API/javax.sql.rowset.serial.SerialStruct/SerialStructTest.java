/*
 * @test
 * @summary javax.sql.rowset.serial.SerialStruct conformance: sqlTypeName/attributes.
 * @run main SerialStructTest
 */


public class SerialStructTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static java.sql.Struct mkStruct() {
        return new java.sql.Struct() {
            public String getSQLTypeName() { return "T"; }
            public Object[] getAttributes() { return new Object[]{"a", Integer.valueOf(1)}; }
            public Object[] getAttributes(java.util.Map<String, Class<?>> m) { return getAttributes(); }
        };
    }
    public static void main(String[] args) throws Exception {
        javax.sql.rowset.serial.SerialStruct s = new javax.sql.rowset.serial.SerialStruct(mkStruct(), new java.util.HashMap<String, Class<?>>());
        check("T".equals(s.getSQLTypeName()), "getSQLTypeName");
        Object[] a = s.getAttributes();
        check(a.length == 2 && "a".equals(a[0]), "getAttributes");        System.out.println("PASS (" + passed + " assertions)");
    }
}
