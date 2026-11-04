/*
 * @test
 * @summary javax.swing.text.AsyncBoxView.ChildState conformance: type shape via reflection (headless-safe).
 * @run main ChildStateTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class ChildStateTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.text.AsyncBoxView$ChildState", false, ChildStateTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(hasInterface(c, "java.lang.Runnable"), "implements java.lang.Runnable");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getChildView"), "method getChildView");
        check(names.contains("run"), "method run");
        check(names.contains("getMinorSpan"), "method getMinorSpan");
        check(names.contains("getMinorOffset"), "method getMinorOffset");
        check(names.contains("getMajorSpan"), "method getMajorSpan");
        check(names.contains("getMajorOffset"), "method getMajorOffset");
        check(names.contains("setMajorOffset"), "method setMajorOffset");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
