/*
 * @test
 * @summary com.sun.java.accessibility.util.AWTEventMonitor conformance: AWT listener registry methods.
 * @run main AWTEventMonitorTest
 */

import java.lang.reflect.*;
import java.util.*;

public class AWTEventMonitorTest {
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
        Class<?> c = load("com.sun.java.accessibility.util.AWTEventMonitor");
        check(!c.isInterface(), "not an interface");
        Set<String> m = methodNames(c);
        check(m.contains("getComponentWithFocus"), "getComponentWithFocus");
        check(m.contains("addComponentListener"), "addComponentListener");
        check(m.contains("removeComponentListener"), "removeComponentListener");
        check(m.contains("addContainerListener"), "addContainerListener");
        check(m.contains("addFocusListener"), "addFocusListener");
        check(m.contains("addKeyListener"), "addKeyListener");
        check(m.contains("addMouseListener"), "addMouseListener");
        check(m.contains("addWindowListener"), "addWindowListener");
        check(m.contains("addActionListener"), "addActionListener");
        check(m.contains("addTextListener"), "addTextListener");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
