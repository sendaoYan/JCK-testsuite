/*
 * @test
 * @summary javax.swing.text.JTextComponent.AccessibleJTextComponent conformance: type shape and supertypes.
 * @run main AccessibleJTextComponentTest
 */

import java.lang.reflect.Modifier;

public class AccessibleJTextComponentTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.text.JTextComponent$AccessibleJTextComponent");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.JComponent$AccessibleJComponent"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 6, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.accessibility.AccessibleText"), "interface 0");
        check(c.getInterfaces()[1].getName().equals("javax.swing.event.CaretListener"), "interface 1");
        check(c.getInterfaces()[2].getName().equals("javax.swing.event.DocumentListener"), "interface 2");
        check(c.getInterfaces()[3].getName().equals("javax.accessibility.AccessibleAction"), "interface 3");
        check(c.getInterfaces()[4].getName().equals("javax.accessibility.AccessibleEditableText"), "interface 4");
        check(c.getInterfaces()[5].getName().equals("javax.accessibility.AccessibleExtendedText"), "interface 5");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
