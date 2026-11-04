/*
 * @test
 * @summary javax.swing.SwingUtilities conformance: type shape via reflection (headless-safe).
 * @run main SwingUtilitiesTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class SwingUtilitiesTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.SwingUtilities", false, SwingUtilitiesTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(hasInterface(c, "javax.swing.SwingConstants"), "implements javax.swing.SwingConstants");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("isRectangleContainingRectangle"), "method isRectangleContainingRectangle");
        check(names.contains("getLocalBounds"), "method getLocalBounds");
        check(names.contains("getWindowAncestor"), "method getWindowAncestor");
        check(names.contains("convertPoint"), "method convertPoint");
        check(names.contains("convertPoint"), "method convertPoint");
        check(names.contains("convertRectangle"), "method convertRectangle");
        check(names.contains("getAncestorOfClass"), "method getAncestorOfClass");
        check(names.contains("getAncestorNamed"), "method getAncestorNamed");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
