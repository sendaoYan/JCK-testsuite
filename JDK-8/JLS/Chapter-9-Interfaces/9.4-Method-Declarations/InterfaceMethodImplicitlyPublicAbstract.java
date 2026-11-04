/*
 * @test
 * @summary JLS 9.4 (Java SE 8): an interface method without a body is implicitly
 *          public and abstract.
 * @run main InterfaceMethodImplicitlyPublicAbstract
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class InterfaceMethodImplicitlyPublicAbstract {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface I {
        void m();   // implicitly public abstract
    }

    public static void main(String[] args) throws Exception {
        Method m = I.class.getMethod("m");
        check(Modifier.isPublic(m.getModifiers()), "method is implicitly public");
        check(Modifier.isAbstract(m.getModifiers()), "method is implicitly abstract");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
