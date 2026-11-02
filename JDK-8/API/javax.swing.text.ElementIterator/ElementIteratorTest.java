/*
 * @test
 * @summary javax.swing.text.ElementIterator conformance: type shape via reflection (headless-safe).
 * @run main ElementIteratorTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class ElementIteratorTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.text.ElementIterator", false, ElementIteratorTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(hasInterface(c, "java.lang.Cloneable"), "implements java.lang.Cloneable");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("clone"), "method clone");
        check(names.contains("first"), "method first");
        check(names.contains("depth"), "method depth");
        check(names.contains("current"), "method current");
        check(names.contains("next"), "method next");
        check(names.contains("previous"), "method previous");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
