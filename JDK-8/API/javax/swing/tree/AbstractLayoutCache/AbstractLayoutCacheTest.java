/*
 * @test
 * @summary javax.swing.tree.AbstractLayoutCache conformance: type shape via reflection (headless-safe).
 * @run main AbstractLayoutCacheTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class AbstractLayoutCacheTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.tree.AbstractLayoutCache", false, AbstractLayoutCacheTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(hasInterface(c, "javax.swing.tree.RowMapper"), "implements javax.swing.tree.RowMapper");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("setNodeDimensions"), "method setNodeDimensions");
        check(names.contains("getNodeDimensions"), "method getNodeDimensions");
        check(names.contains("setModel"), "method setModel");
        check(names.contains("getModel"), "method getModel");
        check(names.contains("setRootVisible"), "method setRootVisible");
        check(names.contains("isRootVisible"), "method isRootVisible");
        check(names.contains("setRowHeight"), "method setRowHeight");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
