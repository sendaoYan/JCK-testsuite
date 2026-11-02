/*
 * @test
 * @summary com.sun.java.accessibility.util.SwingEventMonitor conformance: Swing listener registry.
 * @run main SwingEventMonitorTest
 */

import java.lang.reflect.*;
import java.util.*;

public class SwingEventMonitorTest {
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

    public static void main(String[] a) throws Exception {
        Class<?> c = load("com.sun.java.accessibility.util.SwingEventMonitor");
        check(!c.isInterface(), "not an interface");
        check(c.getSuperclass().getName().equals("com.sun.java.accessibility.util.AWTEventMonitor"), "extends AWTEventMonitor");
        Set<String> m = methodNames(c);
        check(m.contains("addAncestorListener"), "addAncestorListener");
        check(m.contains("addCaretListener"), "addCaretListener");
        check(m.contains("addCellEditorListener"), "addCellEditorListener");
        check(m.contains("addChangeListener"), "addChangeListener");
        check(m.contains("addDocumentListener"), "addDocumentListener");
        check(m.contains("addListSelectionListener"), "addListSelectionListener");
        check(m.contains("addPropertyChangeListener"), "addPropertyChangeListener");
        check(m.contains("addVetoableChangeListener"), "addVetoableChangeListener");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
