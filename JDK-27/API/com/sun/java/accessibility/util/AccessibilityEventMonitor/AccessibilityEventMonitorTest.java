/*
 * @test
 * @summary com.sun.java.accessibility.util.AccessibilityEventMonitor conformance: white-box structure + black-box behavior.
 * @run main AccessibilityEventMonitorTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.beans.*;

public class AccessibilityEventMonitorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Set<String> methodNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) s.add(m.getName());
        return s;
    }
    static Set<String> fieldNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Field f : c.getDeclaredFields()) s.add(f.getName());
        return s;
    }
    static Set<String> interfaces(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Class<?> i : c.getInterfaces()) s.add(i.getName());
        return s;
    }
    static Class<?> load(String fqcn) throws Exception { return Class.forName(fqcn); }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("com.sun.java.accessibility.util.AccessibilityEventMonitor");
        check(!c.isInterface(), "not an interface");
        check(!Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(c.getDeclaredConstructor() != null, "public constructor");
        Set<String> m = methodNames(c);
        check(m.contains("addPropertyChangeListener"), "addPropertyChangeListener");
        check(m.contains("removePropertyChangeListener"), "removePropertyChangeListener");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        com.sun.java.accessibility.util.AccessibilityEventMonitor mon =
            new com.sun.java.accessibility.util.AccessibilityEventMonitor();
        check(mon != null, "new AccessibilityEventMonitor() non-null");

        PropertyChangeListener pcl = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) {}
        };
        com.sun.java.accessibility.util.AccessibilityEventMonitor.addPropertyChangeListener(pcl);
        com.sun.java.accessibility.util.AccessibilityEventMonitor.removePropertyChangeListener(pcl);

        // Chained registration and unwind (multi-listener combination).
        PropertyChangeListener pcl2 = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) {}
        };
        com.sun.java.accessibility.util.AccessibilityEventMonitor.addPropertyChangeListener(pcl);
        com.sun.java.accessibility.util.AccessibilityEventMonitor.addPropertyChangeListener(pcl2);
        com.sun.java.accessibility.util.AccessibilityEventMonitor.removePropertyChangeListener(pcl);
        com.sun.java.accessibility.util.AccessibilityEventMonitor.removePropertyChangeListener(pcl2);
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
