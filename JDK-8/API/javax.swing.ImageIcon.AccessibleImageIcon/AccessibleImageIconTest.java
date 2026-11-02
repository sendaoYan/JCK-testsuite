/*
 * @test
 * @summary javax.swing.ImageIcon.AccessibleImageIcon conformance: type shape via reflection (headless-safe).
 * @run main AccessibleImageIconTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class AccessibleImageIconTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.ImageIcon$AccessibleImageIcon", false, AccessibleImageIconTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.accessibility.AccessibleContext"), "superclass");
        check(hasInterface(c, "javax.accessibility.AccessibleIcon"), "implements javax.accessibility.AccessibleIcon");
        check(hasInterface(c, "java.io.Serializable"), "implements java.io.Serializable");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getAccessibleRole"), "method getAccessibleRole");
        check(names.contains("getAccessibleStateSet"), "method getAccessibleStateSet");
        check(names.contains("getAccessibleParent"), "method getAccessibleParent");
        check(names.contains("getAccessibleIndexInParent"), "method getAccessibleIndexInParent");
        check(names.contains("getAccessibleChildrenCount"), "method getAccessibleChildrenCount");
        check(names.contains("getAccessibleChild"), "method getAccessibleChild");
        check(names.contains("getLocale"), "method getLocale");
        check(names.contains("getAccessibleIconDescription"), "method getAccessibleIconDescription");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
