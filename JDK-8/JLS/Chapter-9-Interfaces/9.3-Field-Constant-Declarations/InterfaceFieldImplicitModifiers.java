/*
 * @test
 * @summary JLS 9.3 (Java SE 8): every field declaration in an interface is implicitly
 *          public, static, and final.
 * @run main InterfaceFieldImplicitModifiers
 */

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class InterfaceFieldImplicitModifiers {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface I {
        int X = 42;   // implicitly public static final
    }

    public static void main(String[] args) throws Exception {
        Field f = I.class.getField("X");
        int mods = f.getModifiers();
        check(Modifier.isPublic(mods) && Modifier.isStatic(mods) && Modifier.isFinal(mods),
              "field is public static final");
        check(I.X == 42, "field value");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
