/*
 * @test
 * @summary JLS 9.1.1.1 (Java SE 8): every interface is implicitly abstract; the
 *          abstract modifier is obsolete and may be redundantly specified.
 * @run main InterfaceImplicitlyAbstract
 */

import java.lang.reflect.Modifier;

public class InterfaceImplicitlyAbstract {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface I { void m(); }

    public static void main(String[] args) {
        check(Modifier.isAbstract(I.class.getModifiers()), "interface is implicitly abstract");
        check(Modifier.isInterface(I.class.getModifiers()), "interface modifier set");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
