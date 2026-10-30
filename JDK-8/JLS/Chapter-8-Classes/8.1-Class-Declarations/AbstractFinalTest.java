/*
 * @test
 * @summary JLS 8.1.1.1 (Java SE 8): an abstract class cannot be instantiated and
 *          may declare abstract methods implemented by subclasses.
 * @run main AbstractFinalTest
 */

public class AbstractFinalTest {
    abstract static class A { abstract int f(); }
    static class B extends A { int f() { return 1; } }
    final static class C { int g() { return 2; } }

    public static void main(String[] args) {
        A a = new B();
        if (a.f() != 1) throw new AssertionError("abstract method dispatch");
        C c = new C();
        if (c.g() != 2) throw new AssertionError("final class instance");
        System.out.println("PASS");
    }
}
