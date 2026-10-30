/*
 * @test
 * @summary JLS 9.4.1.1: an instance method declared in a subinterface overrides
 *          a default method from a superinterface when its signature is a
 *          subsignature.
 * @run main InterfaceMethodOverriding
 */

public class InterfaceMethodOverriding {
    interface Top { default String name() { return "unnamed"; } }
    interface Bottom extends Top {
        default String name() { return "bottom"; }   // overrides Top.name
    }
    static class Impl implements Bottom {}

    public static void main(String[] args) {
        Impl i = new Impl();
        if (!"bottom".equals(i.name())) throw new AssertionError("overriding");
        System.out.println("PASS");
    }
}
