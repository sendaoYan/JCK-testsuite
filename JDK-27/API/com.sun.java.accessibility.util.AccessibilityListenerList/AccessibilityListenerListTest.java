/*
 * @test
 * @summary com.sun.java.accessibility.util.AccessibilityListenerList conformance: add/remove/count.
 * @run main AccessibilityListenerListTest
 */

import java.lang.reflect.*;
import java.util.*;

public class AccessibilityListenerListTest {
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
        Class<?> c = load("com.sun.java.accessibility.util.AccessibilityListenerList");
        check(!c.isInterface(), "not an interface");
        com.sun.java.accessibility.util.AccessibilityListenerList l = new com.sun.java.accessibility.util.AccessibilityListenerList();
        check(l.getListenerCount() == 0, "initial count 0");
        check(l.getListenerList() != null, "list non-null");
        java.util.EventListener el = new java.util.EventListener() {};
        l.add(java.util.EventListener.class, el);
        check(l.getListenerCount() == 1, "count 1 after add");
        check(l.getListenerCount(java.util.EventListener.class) == 1, "typed count 1");
        l.remove(java.util.EventListener.class, el);
        check(l.getListenerCount() == 0, "count 0 after remove");
        check(l.toString() != null, "toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
