/*
 * @test
 * @summary JLS 9.3: every field declaration in an interface is implicitly
 *          public, static, and final.
 * @run main InterfaceFieldImplicitModifiers
 */

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class InterfaceFieldImplicitModifiers {
    interface I {
        int X = 42;   // implicitly public static final
    }

    public static void main(String[] args) throws Exception {
        Field f = I.class.getField("X");
        int mods = f.getModifiers();
        if (!Modifier.isPublic(mods) || !Modifier.isStatic(mods) || !Modifier.isFinal(mods))
            throw new AssertionError("implicit modifiers: " + Modifier.toString(mods));
        if (I.X != 42) throw new AssertionError("value: " + I.X);
        System.out.println("PASS");
    }
}
