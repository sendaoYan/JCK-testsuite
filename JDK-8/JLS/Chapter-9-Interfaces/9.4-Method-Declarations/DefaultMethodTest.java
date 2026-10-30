/*
 * @test
 * @summary JLS 9.4.3 (Java SE 8): a default method provides an implementation that
 *          an implementing class inherits unless it overrides it.
 * @run main DefaultMethodTest
 */

public class DefaultMethodTest {
    interface Greeter {
        default String greet() { return "hello"; }
    }
    static class A implements Greeter {}
    static class B implements Greeter {
        public String greet() { return "hi"; }
    }

    public static void main(String[] args) {
        Greeter a = new A();
        if (!a.greet().equals("hello")) throw new AssertionError("inherited default method");
        Greeter b = new B();
        if (!b.greet().equals("hi")) throw new AssertionError("overridden default method");
        System.out.println("PASS");
    }
}
