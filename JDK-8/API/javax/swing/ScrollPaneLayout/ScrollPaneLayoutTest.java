/*
 * @test
 * @summary javax.swing.ScrollPaneLayout conformance: type shape via reflection (headless-safe).
 * @run main ScrollPaneLayoutTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class ScrollPaneLayoutTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.ScrollPaneLayout", false, ScrollPaneLayoutTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(hasInterface(c, "java.awt.LayoutManager"), "implements java.awt.LayoutManager");
        check(hasInterface(c, "javax.swing.ScrollPaneConstants"), "implements javax.swing.ScrollPaneConstants");
        check(hasInterface(c, "java.io.Serializable"), "implements java.io.Serializable");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("syncWithScrollPane"), "method syncWithScrollPane");
        check(names.contains("addLayoutComponent"), "method addLayoutComponent");
        check(names.contains("removeLayoutComponent"), "method removeLayoutComponent");
        check(names.contains("getVerticalScrollBarPolicy"), "method getVerticalScrollBarPolicy");
        check(names.contains("setVerticalScrollBarPolicy"), "method setVerticalScrollBarPolicy");
        check(names.contains("getHorizontalScrollBarPolicy"), "method getHorizontalScrollBarPolicy");
        check(names.contains("setHorizontalScrollBarPolicy"), "method setHorizontalScrollBarPolicy");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
