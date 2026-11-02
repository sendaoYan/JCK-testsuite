/*
 * @test
 * @summary javax.swing.DefaultButtonModel conformance: type shape and supertypes.
 * @run main DefaultButtonModelTest
 */

import java.lang.reflect.Modifier;

public class DefaultButtonModelTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.DefaultButtonModel");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 2, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.swing.ButtonModel"), "interface 0");
        check(c.getInterfaces()[1].getName().equals("java.io.Serializable"), "interface 1");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
