/*
 * @test
 * @summary javax.swing.GroupLayout conformance: type shape via reflection (headless-safe).
 * @run main GroupLayoutTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class GroupLayoutTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.GroupLayout", false, GroupLayoutTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(hasInterface(c, "java.awt.LayoutManager2"), "implements java.awt.LayoutManager2");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("setHonorsVisibility"), "method setHonorsVisibility");
        check(names.contains("getHonorsVisibility"), "method getHonorsVisibility");
        check(names.contains("setHonorsVisibility"), "method setHonorsVisibility");
        check(names.contains("setAutoCreateGaps"), "method setAutoCreateGaps");
        check(names.contains("getAutoCreateGaps"), "method getAutoCreateGaps");
        check(names.contains("setAutoCreateContainerGaps"), "method setAutoCreateContainerGaps");
        check(names.contains("getAutoCreateContainerGaps"), "method getAutoCreateContainerGaps");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
