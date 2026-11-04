/*
 * @test
 * @summary javax.swing.text.View conformance: type shape via reflection (headless-safe).
 * @run main ViewTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class ViewTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.text.View", false, ViewTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(hasInterface(c, "javax.swing.SwingConstants"), "implements javax.swing.SwingConstants");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getParent"), "method getParent");
        check(names.contains("isVisible"), "method isVisible");
        check(names.contains("getPreferredSpan"), "method getPreferredSpan");
        check(names.contains("getMinimumSpan"), "method getMinimumSpan");
        check(names.contains("getMaximumSpan"), "method getMaximumSpan");
        check(names.contains("preferenceChanged"), "method preferenceChanged");
        check(names.contains("getAlignment"), "method getAlignment");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
