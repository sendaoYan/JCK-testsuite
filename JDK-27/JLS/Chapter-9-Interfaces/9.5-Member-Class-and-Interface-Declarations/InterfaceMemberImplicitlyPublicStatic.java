/*
 * @test
 * @summary JLS 9.5: every member class or interface declared in an interface is
 *          implicitly public and static.
 * @run main InterfaceMemberImplicitlyPublicStatic
 */

public class InterfaceMemberImplicitlyPublicStatic {
    interface I {
        class C { int v() { return 1; } }   // implicitly public static
        interface J { int v(); }
    }

    public static void main(String[] args) {
        I.C c = new I.C();   // accessible and instantiable without an enclosing instance
        if (c.v() != 1) throw new AssertionError("member class");
        System.out.println("PASS");
    }
}
