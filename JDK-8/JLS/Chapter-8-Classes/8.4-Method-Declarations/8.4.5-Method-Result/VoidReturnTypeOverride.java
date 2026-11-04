/*
 * @test
 * @summary JLS 8.4.5 (Java SE 8): void is return-type-substitutable for void, so a void
 *          method overrides another void method.
 * @run main VoidReturnTypeOverride
 */
class Base { void m() { } }
public class VoidReturnTypeOverride extends Base {
    @Override void m() { }
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    public static void main(String[] args) {
        new VoidReturnTypeOverride().m();
        check(true, "void return type overrides void");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
