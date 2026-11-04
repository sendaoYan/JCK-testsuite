/*
 * @test
 * @summary JLS 8.4.9 (Java SE 8): a method name is overloaded when one declaration is
 *          inherited and another is declared, with non-override-equivalent
 *          signatures.
 * @run main OverloadingInherited
 */
class Base { String f(int x) { return "int"; } }
public class OverloadingInherited extends Base {
    String f(String x) { return "String"; }
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    public static void main(String[] args) {
        OverloadingInherited o = new OverloadingInherited();
        check(o.f(1).equals("int"), "inherited overload");
        check(o.f("x").equals("String"), "declared overload");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
