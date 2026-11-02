/*
 * @test
 * @summary java.sql.DriverPropertyInfo conformance: public fields and constructor
 * @run main DriverPropertyInfoTest
 */

public class DriverPropertyInfoTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        java.sql.DriverPropertyInfo p = new java.sql.DriverPropertyInfo("name", "desc");
        check("name".equals(p.name), "name field");
        check("desc".equals(p.value), "value field from constructor");
        check(p.description == null, "description default null");
        check(p.required == false, "required default false");
        check(p.choices == null, "choices default null");
        p.description = "desc";
        p.required = true;
        p.value = "v";
        p.choices = new String[]{"a", "b"};
        check("desc".equals(p.description) && p.required && "v".equals(p.value) && p.choices.length == 2, "mutable fields");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
