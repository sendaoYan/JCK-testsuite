/*
 * @test
 * @summary javax.swing.AbstractButton conformance: type shape via reflection (headless-safe).
 * @run main AbstractButtonTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class AbstractButtonTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.AbstractButton", false, AbstractButtonTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.JComponent"), "superclass");
        check(hasInterface(c, "java.awt.ItemSelectable"), "implements java.awt.ItemSelectable");
        check(hasInterface(c, "javax.swing.SwingConstants"), "implements javax.swing.SwingConstants");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("setHideActionText"), "method setHideActionText");
        check(names.contains("getHideActionText"), "method getHideActionText");
        check(names.contains("getText"), "method getText");
        check(names.contains("setText"), "method setText");
        check(names.contains("isSelected"), "method isSelected");
        check(names.contains("setSelected"), "method setSelected");
        check(names.contains("doClick"), "method doClick");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
