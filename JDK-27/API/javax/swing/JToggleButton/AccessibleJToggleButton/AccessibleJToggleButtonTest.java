/*
 * @test
 * @summary javax.swing.JToggleButton.AccessibleJToggleButton conformance: type shape and supertypes.
 * @run main AccessibleJToggleButtonTest
 */

import java.lang.reflect.Modifier;

public class AccessibleJToggleButtonTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.JToggleButton$AccessibleJToggleButton");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == false, "public modifier");
        check(Modifier.isProtected(mods) == true, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.AbstractButton$AccessibleAbstractButton"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("java.awt.event.ItemListener"), "interface 0");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
