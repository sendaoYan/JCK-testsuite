/*
 * @test
 * @summary JLS 9.1.1.3: a nested interface is implicitly static, so it may be
 *          referenced from a static context without an enclosing instance.
 * @run main NestedInterfaceImplicitlyStatic
 */

class Outer {
    interface Nested { int v(); }   // implicitly static
}

public class NestedInterfaceImplicitlyStatic {
    static Outer.Nested n = () -> 42;

    public static void main(String[] args) {
        if (n.v() != 42) throw new AssertionError("nested interface");
        System.out.println("PASS");
    }
}
