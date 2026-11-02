/*
 * @test
 * @summary java.sql.DriverPropertyInfo conformance: fields.
 * @run main DriverPropertyInfoTest
 */


public class DriverPropertyInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        java.sql.DriverPropertyInfo p = new java.sql.DriverPropertyInfo("user", "sa");
        check("user".equals(p.name), "name");
        check("sa".equals(p.value), "value");
        check(!p.required, "required false");
        check(p.description == null, "description null");
        check(p.choices == null, "choices null");        System.out.println("PASS (" + passed + " assertions)");
    }
}
