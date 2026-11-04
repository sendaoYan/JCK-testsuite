/*
 * @test
 * @summary javax.swing.JMenuBar.AccessibleJMenuBar conformance: type shape via reflection (headless-safe).
 * @run main AccessibleJMenuBarTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class AccessibleJMenuBarTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.JMenuBar$AccessibleJMenuBar", false, AccessibleJMenuBarTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.JComponent$AccessibleJComponent"), "superclass");
        check(hasInterface(c, "javax.accessibility.AccessibleSelection"), "implements javax.accessibility.AccessibleSelection");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getAccessibleStateSet"), "method getAccessibleStateSet");
        check(names.contains("getAccessibleRole"), "method getAccessibleRole");
        check(names.contains("getAccessibleSelection"), "method getAccessibleSelection");
        check(names.contains("getAccessibleSelectionCount"), "method getAccessibleSelectionCount");
        check(names.contains("getAccessibleSelection"), "method getAccessibleSelection");
        check(names.contains("isAccessibleChildSelected"), "method isAccessibleChildSelected");
        check(names.contains("addAccessibleSelection"), "method addAccessibleSelection");
        check(names.contains("removeAccessibleSelection"), "method removeAccessibleSelection");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
