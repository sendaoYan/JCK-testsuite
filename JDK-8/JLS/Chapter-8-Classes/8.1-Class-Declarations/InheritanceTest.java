/*
 * @test
 * @summary JLS 8.1.4/8.1.5 (Java SE 8): a class extends one superclass (Object by
 *          default) and may implement any number of interfaces.
 * @run main InheritanceTest
 */

public class InheritanceTest {
    interface I { int f(); }
    static class A { int a() { return 1; } }
    static class B extends A implements I {
        public int f() { return 2; }
    }

    public static void main(String[] args) {
        B b = new B();
        if (b.a() != 1) throw new AssertionError("inherited method");
        if (b.f() != 2) throw new AssertionError("implemented method");
        if (!(b instanceof A) || !(b instanceof I)) throw new AssertionError("subtype relations");
        if (b.getClass().getSuperclass() != A.class) throw new AssertionError("superclass");
        if (new Object().getClass().getSuperclass() != null) throw new AssertionError("Object has no superclass");
        System.out.println("PASS");
    }
}
