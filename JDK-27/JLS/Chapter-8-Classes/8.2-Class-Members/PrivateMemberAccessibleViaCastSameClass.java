/*
 * @test
 * @summary JLS 8.2: a private field is never inherited, but may be accessible to
 *          a subclass when both classes are members of the same class, via a
 *          cast to the superclass type.
 * @run main PrivateMemberAccessibleViaCastSameClass
 */
public class PrivateMemberAccessibleViaCastSameClass {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    class A { private int x = 5; }
    class B extends A { int read() { return ((A) this).x; } }
    public static void main(String[] args) {
        PrivateMemberAccessibleViaCastSameClass o = new PrivateMemberAccessibleViaCastSameClass();
        check(o.new B().read() == 5, "private superclass field accessible via cast within same enclosing class");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
