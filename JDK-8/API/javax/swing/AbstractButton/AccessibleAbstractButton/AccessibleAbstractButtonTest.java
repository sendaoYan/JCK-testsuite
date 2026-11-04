/*
 * @test
 * @summary javax.swing.AbstractButton.AccessibleAbstractButton conformance: type shape via reflection (headless-safe).
 * @run main AccessibleAbstractButtonTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class AccessibleAbstractButtonTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.AbstractButton$AccessibleAbstractButton", false, AccessibleAbstractButtonTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.JComponent$AccessibleJComponent"), "superclass");
        check(hasInterface(c, "javax.accessibility.AccessibleAction"), "implements javax.accessibility.AccessibleAction");
        check(hasInterface(c, "javax.accessibility.AccessibleValue"), "implements javax.accessibility.AccessibleValue");
        check(hasInterface(c, "javax.accessibility.AccessibleText"), "implements javax.accessibility.AccessibleText");
        check(hasInterface(c, "javax.accessibility.AccessibleExtendedComponent"), "implements javax.accessibility.AccessibleExtendedComponent");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getAccessibleName"), "method getAccessibleName");
        check(names.contains("getAccessibleIcon"), "method getAccessibleIcon");
        check(names.contains("getAccessibleStateSet"), "method getAccessibleStateSet");
        check(names.contains("getAccessibleRelationSet"), "method getAccessibleRelationSet");
        check(names.contains("getAccessibleAction"), "method getAccessibleAction");
        check(names.contains("getAccessibleValue"), "method getAccessibleValue");
        check(names.contains("getAccessibleActionCount"), "method getAccessibleActionCount");
        check(names.contains("getAccessibleActionDescription"), "method getAccessibleActionDescription");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
