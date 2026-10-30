/*
 * @test
 * @summary JLS 8.4.8.2 (Java SE 8): a static method is hidden, not overridden; the
 *          invoked method is chosen by the compile-time type of the reference.
 * @run main StaticVsInstanceTest
 */

public class StaticVsInstanceTest {
    static class A { static String f() { return "A"; } }
    static class B extends A { static String f() { return "B"; } }

    public static void main(String[] args) {
        A a = new B();
        if (!a.f().equals("A")) throw new AssertionError("static method hidden, not overridden");
        if (!B.f().equals("B")) throw new AssertionError("subclass static method");
        System.out.println("PASS");
    }
}
