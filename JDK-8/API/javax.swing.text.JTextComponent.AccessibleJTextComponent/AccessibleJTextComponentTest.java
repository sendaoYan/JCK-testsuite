/*
 * @test
 * @summary javax.swing.text.JTextComponent.AccessibleJTextComponent conformance: type shape via reflection (headless-safe).
 * @run main AccessibleJTextComponentTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class AccessibleJTextComponentTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.text.JTextComponent$AccessibleJTextComponent", false, AccessibleJTextComponentTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.JComponent$AccessibleJComponent"), "superclass");
        check(hasInterface(c, "javax.accessibility.AccessibleText"), "implements javax.accessibility.AccessibleText");
        check(hasInterface(c, "javax.swing.event.CaretListener"), "implements javax.swing.event.CaretListener");
        check(hasInterface(c, "javax.swing.event.DocumentListener"), "implements javax.swing.event.DocumentListener");
        check(hasInterface(c, "javax.accessibility.AccessibleAction"), "implements javax.accessibility.AccessibleAction");
        check(hasInterface(c, "javax.accessibility.AccessibleEditableText"), "implements javax.accessibility.AccessibleEditableText");
        check(hasInterface(c, "javax.accessibility.AccessibleExtendedText"), "implements javax.accessibility.AccessibleExtendedText");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("caretUpdate"), "method caretUpdate");
        check(names.contains("insertUpdate"), "method insertUpdate");
        check(names.contains("removeUpdate"), "method removeUpdate");
        check(names.contains("changedUpdate"), "method changedUpdate");
        check(names.contains("getAccessibleStateSet"), "method getAccessibleStateSet");
        check(names.contains("getAccessibleRole"), "method getAccessibleRole");
        check(names.contains("getAccessibleText"), "method getAccessibleText");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
