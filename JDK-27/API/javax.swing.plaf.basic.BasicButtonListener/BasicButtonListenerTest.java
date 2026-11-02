/*
 * @test
 * @summary javax.swing.plaf.basic.BasicButtonListener conformance: type shape and supertypes.
 * @run main BasicButtonListenerTest
 */

import java.lang.reflect.Modifier;

public class BasicButtonListenerTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.plaf.basic.BasicButtonListener");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 5, "interface count");
        check(c.getInterfaces()[0].getName().equals("java.awt.event.MouseListener"), "interface 0");
        check(c.getInterfaces()[1].getName().equals("java.awt.event.MouseMotionListener"), "interface 1");
        check(c.getInterfaces()[2].getName().equals("java.awt.event.FocusListener"), "interface 2");
        check(c.getInterfaces()[3].getName().equals("javax.swing.event.ChangeListener"), "interface 3");
        check(c.getInterfaces()[4].getName().equals("java.beans.PropertyChangeListener"), "interface 4");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
