/*
 * @test
 * @summary javax.swing.ProgressMonitor.AccessibleProgressMonitor conformance: type shape via reflection (headless-safe).
 * @run main AccessibleProgressMonitorTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class AccessibleProgressMonitorTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.ProgressMonitor$AccessibleProgressMonitor", false, AccessibleProgressMonitorTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.accessibility.AccessibleContext"), "superclass");
        check(hasInterface(c, "javax.accessibility.AccessibleText"), "implements javax.accessibility.AccessibleText");
        check(hasInterface(c, "javax.swing.event.ChangeListener"), "implements javax.swing.event.ChangeListener");
        check(hasInterface(c, "java.beans.PropertyChangeListener"), "implements java.beans.PropertyChangeListener");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("stateChanged"), "method stateChanged");
        check(names.contains("propertyChange"), "method propertyChange");
        check(names.contains("getAccessibleName"), "method getAccessibleName");
        check(names.contains("getAccessibleDescription"), "method getAccessibleDescription");
        check(names.contains("getAccessibleRole"), "method getAccessibleRole");
        check(names.contains("getAccessibleStateSet"), "method getAccessibleStateSet");
        check(names.contains("getAccessibleParent"), "method getAccessibleParent");
        check(names.contains("getAccessibleIndexInParent"), "method getAccessibleIndexInParent");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
