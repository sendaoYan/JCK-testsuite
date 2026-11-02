/*
 * @test
 * @summary javax.swing.ScrollPaneLayout conformance: type shape and supertypes.
 * @run main ScrollPaneLayoutTest
 */

import java.lang.reflect.Modifier;

public class ScrollPaneLayoutTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.ScrollPaneLayout");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 3, "interface count");
        check(c.getInterfaces()[0].getName().equals("java.awt.LayoutManager"), "interface 0");
        check(c.getInterfaces()[1].getName().equals("javax.swing.ScrollPaneConstants"), "interface 1");
        check(c.getInterfaces()[2].getName().equals("java.io.Serializable"), "interface 2");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
