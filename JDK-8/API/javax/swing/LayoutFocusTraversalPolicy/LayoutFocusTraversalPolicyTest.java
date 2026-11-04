/*
 * @test
 * @summary javax.swing.LayoutFocusTraversalPolicy conformance: type shape via reflection (headless-safe).
 * @run main LayoutFocusTraversalPolicyTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class LayoutFocusTraversalPolicyTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.LayoutFocusTraversalPolicy", false, LayoutFocusTraversalPolicyTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.SortingFocusTraversalPolicy"), "superclass");
        check(hasInterface(c, "java.io.Serializable"), "implements java.io.Serializable");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getComponentAfter"), "method getComponentAfter");
        check(names.contains("getComponentBefore"), "method getComponentBefore");
        check(names.contains("getFirstComponent"), "method getFirstComponent");
        check(names.contains("getLastComponent"), "method getLastComponent");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
