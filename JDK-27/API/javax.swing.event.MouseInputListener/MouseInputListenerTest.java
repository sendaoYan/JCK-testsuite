/*
 * @test
 * @summary javax.swing.event.MouseInputListener conformance: type shape and supertypes.
 * @run main MouseInputListenerTest
 */

import java.lang.reflect.Modifier;

public class MouseInputListenerTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.event.MouseInputListener");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == true, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 2, "interface count");
        check(c.getInterfaces()[0].getName().equals("java.awt.event.MouseListener"), "interface 0");
        check(c.getInterfaces()[1].getName().equals("java.awt.event.MouseMotionListener"), "interface 1");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
