/*
 * @test
 * @summary JLS 18.5.4 / 15.12.2.5 (Java SE 8): among applicable overloads, the
 *          most specific one is chosen; String is more specific than Object, and
 *          null selects the most specific applicable overload.
 * @run main MostSpecificSubtype
 */

public class MostSpecificSubtype {
    static int passed = 0;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static String f(Object o) { return "Object"; }
    static String f(String s) { return "String"; }

    public static void main(String[] args) {
        check("String".equals(f("x")), "String overload more specific than Object");
        check("String".equals(f((String) null)), "null resolves to the most specific overload");
        check("Object".equals(f(1)), "non-String argument selects Object overload");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
