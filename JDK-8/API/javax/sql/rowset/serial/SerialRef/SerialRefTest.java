/*
 * @test
 * @summary javax.sql.rowset.serial.SerialRef conformance: baseTypeName/getObject.
 * @run main SerialRefTest
 */


public class SerialRefTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static java.sql.Ref mkRef() {
        return new java.sql.Ref() {
            public String getBaseTypeName() { return "VARCHAR"; }
            public Object getObject(java.util.Map<String, Class<?>> m) { return "x"; }
            public Object getObject() { return "x"; }
            public void setObject(Object o) {}
        };
    }
    public static void main(String[] args) throws Exception {
        javax.sql.rowset.serial.SerialRef r = new javax.sql.rowset.serial.SerialRef(mkRef());
        check("VARCHAR".equals(r.getBaseTypeName()), "getBaseTypeName");
        check("x".equals(r.getObject()), "getObject");        System.out.println("PASS (" + passed + " assertions)");
    }
}
