/*
 * @test
 * @summary JLS 9.4.1 (Java SE 8): a superclass method wins over an inherited default
 *          method; when two interfaces provide conflicting defaults, a class must
 *          override the method.
 * @run main MultipleInheritanceTest
 */

public class MultipleInheritanceTest {
    interface A { default String f() { return "A"; } }
    interface B { default String f() { return "B"; } }
    static class Parent { public String f() { return "Parent"; } }
    static class C extends Parent implements A, B {}
    static class D implements A, B {
        public String f() { return "D"; }   // resolves the conflict
    }

    public static void main(String[] args) {
        C c = new C();
        if (!c.f().equals("Parent")) throw new AssertionError("class method wins over default");
        D d = new D();
        if (!d.f().equals("D")) throw new AssertionError("class resolves interface conflict");
        System.out.println("PASS");
    }
}
