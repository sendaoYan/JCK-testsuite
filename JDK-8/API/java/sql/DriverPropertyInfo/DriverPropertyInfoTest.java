/*
 * @test
 * @summary java.sql.DriverPropertyInfo API conformance: full contract coverage.
 *          constructor (name,value) field initialization, public-field defaults
 *          (description null, required false, choices null), field mutability,
 *          and null name/value acceptance.
 * @run main DriverPropertyInfoTest
 */

public class DriverPropertyInfoTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        testConstructor();
        testDefaults();
        testMutability();
        testNullArgs();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testConstructor() {
        java.sql.DriverPropertyInfo p = new java.sql.DriverPropertyInfo("user", "sa");
        check("user".equals(p.name), "name");
        check("sa".equals(p.value), "value");
        check(p.description == null, "description null");
        check(!p.required, "required false");
        check(p.choices == null, "choices null");
    }

    static void testDefaults() {
        java.sql.DriverPropertyInfo p = new java.sql.DriverPropertyInfo("x", "y");
        check(p.name != null, "name non-null");
        check(!p.required, "required defaults false");
    }

    static void testMutability() {
        java.sql.DriverPropertyInfo p = new java.sql.DriverPropertyInfo("a", "b");
        p.name = "renamed";
        p.value = "revalued";
        p.description = "a description";
        p.required = true;
        p.choices = new String[]{"one", "two"};
        check("renamed".equals(p.name), "name mutable");
        check("revalued".equals(p.value), "value mutable");
        check("a description".equals(p.description), "description mutable");
        check(p.required, "required mutable");
        check(p.choices.length == 2 && "two".equals(p.choices[1]), "choices mutable");
    }

    static void testNullArgs() {
        java.sql.DriverPropertyInfo p = new java.sql.DriverPropertyInfo(null, null);
        check(p.name == null, "null name accepted");
        check(p.value == null, "null value accepted");
    }
}
