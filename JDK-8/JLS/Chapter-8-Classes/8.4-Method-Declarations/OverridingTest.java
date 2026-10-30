/*
 * @test
 * @summary JLS 8.4.8.1 (Java SE 8): an instance method overriding a superclass
 *          method is selected by dynamic dispatch based on the runtime type.
 * @run main OverridingTest
 */

public class OverridingTest {
    static class A { String f() { return "A"; } }
    static class B extends A { String f() { return "B"; } }

    public static void main(String[] args) {
        A a = new B();
        if (!a.f().equals("B")) throw new AssertionError("dynamic dispatch");
        A a2 = new A();
        if (!a2.f().equals("A")) throw new AssertionError("base dispatch");
        System.out.println("PASS");
    }
}
