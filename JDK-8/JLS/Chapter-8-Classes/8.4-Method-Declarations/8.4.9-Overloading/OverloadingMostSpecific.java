/*
 * @test
 * @summary JLS 8.4.9 (Java SE 8): overloaded methods are selected at compile time by the
 *          most specific applicable signature for the argument types.
 * @run main OverloadingMostSpecific
 */
public class OverloadingMostSpecific {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    static String f(int x) { return "int"; }
    static String f(long x) { return "long"; }
    static String f(Object x) { return "Object"; }
    static String f(String x) { return "String"; }
    public static void main(String[] args) {
        check(f(1).equals("int"), "int literal selects int overload");
        check(f(1L).equals("long"), "long selects long overload");
        check(f("s").equals("String"), "String selects String overload (more specific than Object)");
        check(f(new Object()).equals("Object"), "Object selects Object overload");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
