/*
 * @test
 * @summary JLS 9.2 (Java SE 8): every member of an interface is implicitly public.
 * @run main InterfaceMemberImplicitlyPublic
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class InterfaceMemberImplicitlyPublic {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface I {
        int X = 1;
        void m();
        class Nested {}
        interface NestedIface {}
    }

    public static void main(String[] args) throws Exception {
        check(Modifier.isPublic(I.class.getField("X").getModifiers()), "field is public");
        Method m = I.class.getMethod("m");
        check(Modifier.isPublic(m.getModifiers()), "method is public");
        check(Modifier.isPublic(I.Nested.class.getModifiers()), "nested class is public");
        check(Modifier.isPublic(I.NestedIface.class.getModifiers()), "nested interface is public");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
