/*
 * @test
 * @summary javax.swing.JSlider.AccessibleJSlider conformance: type shape via reflection (headless-safe).
 * @run main AccessibleJSliderTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class AccessibleJSliderTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.JSlider$AccessibleJSlider", false, AccessibleJSliderTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.JComponent$AccessibleJComponent"), "superclass");
        check(hasInterface(c, "javax.accessibility.AccessibleValue"), "implements javax.accessibility.AccessibleValue");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getAccessibleStateSet"), "method getAccessibleStateSet");
        check(names.contains("getAccessibleRole"), "method getAccessibleRole");
        check(names.contains("getAccessibleValue"), "method getAccessibleValue");
        check(names.contains("getCurrentAccessibleValue"), "method getCurrentAccessibleValue");
        check(names.contains("setCurrentAccessibleValue"), "method setCurrentAccessibleValue");
        check(names.contains("getMinimumAccessibleValue"), "method getMinimumAccessibleValue");
        check(names.contains("getMaximumAccessibleValue"), "method getMaximumAccessibleValue");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
