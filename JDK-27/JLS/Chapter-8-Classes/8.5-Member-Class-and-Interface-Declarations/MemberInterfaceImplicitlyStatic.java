/*
 * @test
 * @summary JLS 8.5: a member interface is implicitly static, so it may be
 *          referred to from a static context without an enclosing instance.
 * @run main MemberInterfaceImplicitlyStatic
 */

class Outer {
    interface Nested { int v(); }   // implicitly static
}

public class MemberInterfaceImplicitlyStatic {
    // Referenced from a static context, proving Nested is implicitly static.
    static Outer.Nested n = () -> 42;

    public static void main(String[] args) {
        if (n.v() != 42) throw new AssertionError("nested interface");
        System.out.println("PASS");
    }
}
