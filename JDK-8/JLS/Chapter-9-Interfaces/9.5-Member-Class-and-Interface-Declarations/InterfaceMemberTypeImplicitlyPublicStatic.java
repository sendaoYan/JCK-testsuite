/*
 * @test
 * @summary JLS 9.5 (Java SE 8): a member class or interface declared in an interface
 *          is implicitly public and static.
 * @run main InterfaceMemberTypeImplicitlyPublicStatic
 */

import java.lang.reflect.Modifier;

public class InterfaceMemberTypeImplicitlyPublicStatic {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface I {
        class Nested { int v() { return 5; } }
        interface NestedIface { int w(); }
    }

    public static void main(String[] args) {
        check(Modifier.isPublic(I.Nested.class.getModifiers()), "member class is public");
        check(Modifier.isStatic(I.Nested.class.getModifiers()), "member class is static");
        check(Modifier.isPublic(I.NestedIface.class.getModifiers()), "member interface is public");
        check(Modifier.isStatic(I.NestedIface.class.getModifiers()), "member interface is static");
        I.Nested n = new I.Nested();
        check(n.v() == 5, "member class usable without enclosing instance");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
