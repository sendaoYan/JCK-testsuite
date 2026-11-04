/*
 * @test
 * @summary javax.swing.JLabel.AccessibleJLabel conformance: type shape and supertypes.
 * @run main AccessibleJLabelTest
 */

import java.lang.reflect.Modifier;

public class AccessibleJLabelTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.JLabel$AccessibleJLabel");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == false, "public modifier");
        check(Modifier.isProtected(mods) == true, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.JComponent$AccessibleJComponent"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 2, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.accessibility.AccessibleText"), "interface 0");
        check(c.getInterfaces()[1].getName().equals("javax.accessibility.AccessibleExtendedComponent"), "interface 1");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
