/*
 * @test
 * @summary com.sun.java.accessibility.util.AccessibilityListenerList conformance: white-box structure + black-box behavior.
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

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("com.sun.java.accessibility.util.AccessibilityListenerList");
        check(!c.isInterface(), "not an interface");
        check(!Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(c.getDeclaredConstructor() != null, "public constructor");
        Set<String> m = methodNames(c);
        check(m.contains("add"), "add");
        check(m.contains("remove"), "remove");
        check(m.contains("getListenerList"), "getListenerList");
        check(m.contains("getListenerCount"), "getListenerCount");
        check(m.contains("toString"), "toString");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        com.sun.java.accessibility.util.AccessibilityListenerList l =
            new com.sun.java.accessibility.util.AccessibilityListenerList();
        check(l.getListenerCount() == 0, "initial count 0");
        check(l.getListenerList() != null, "list non-null");
        check(l.getListenerList().length == 0, "list empty initially");
        check(l.toString().startsWith("EventListenerList:"), "toString prefix");

        java.util.EventListener el = new java.util.EventListener() {};
        java.awt.event.ActionListener al = new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {}
        };

        // add first listener and inspect the raw listener/type pair array.
        l.add(java.util.EventListener.class, el);
        check(l.getListenerCount() == 1, "count 1 after add el");
        check(l.getListenerCount(java.util.EventListener.class) == 1, "typed count el 1");
        check(l.getListenerList().length == 2, "list length 2");
        check(l.getListenerList()[0] == java.util.EventListener.class, "pair[0] is type");
        check(l.getListenerList()[1] == el, "pair[1] is listener");

        // add a second, different-typed listener.
        l.add(java.awt.event.ActionListener.class, al);
        check(l.getListenerCount() == 2, "count 2 after add al");
        check(l.getListenerCount(java.awt.event.ActionListener.class) == 1, "typed count al 1");
        check(l.getListenerCount(java.util.EventListener.class) == 1, "typed count el still 1");
        check(l.getListenerList().length == 4, "list length 4");

        // remove one and verify the other survives.
        l.remove(java.util.EventListener.class, el);
        check(l.getListenerCount() == 1, "count 1 after remove el");
        check(l.getListenerCount(java.util.EventListener.class) == 0, "typed count el 0");
        check(l.getListenerCount(java.awt.event.ActionListener.class) == 1, "typed count al 1");

        l.remove(java.awt.event.ActionListener.class, al);
        check(l.getListenerCount() == 0, "count 0 after remove al");
        check(l.getListenerList().length == 0, "list length back to 0");

        // removing an absent listener is a no-op.
        l.remove(java.util.EventListener.class, el);
        check(l.getListenerCount() == 0, "remove absent is no-op");

        // wrong-type listener -> IllegalArgumentException.
        boolean iae1 = false;
        try { l.add(java.awt.event.ActionListener.class, el); } catch (IllegalArgumentException e) { iae1 = true; }
        check(iae1, "add wrong-type listener throws IAE");

        // null listener -> IllegalArgumentException.
        boolean iae2 = false;
        try { l.add(java.util.EventListener.class, null); } catch (IllegalArgumentException e) { iae2 = true; }
        check(iae2, "add null listener throws IAE");

        boolean iae3 = false;
        try { l.remove(java.util.EventListener.class, null); } catch (IllegalArgumentException e) { iae3 = true; }
        check(iae3, "remove null listener throws IAE");

        check(l.toString().startsWith("EventListenerList:"), "toString prefix after ops");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
