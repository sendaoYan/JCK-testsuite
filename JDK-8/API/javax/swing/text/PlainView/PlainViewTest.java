/*
 * @test
 * @summary javax.swing.text.PlainView conformance: type shape via reflection (headless-safe).
 * @run main PlainViewTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class PlainViewTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.text.PlainView", false, PlainViewTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.text.View"), "superclass");
        check(hasInterface(c, "javax.swing.text.TabExpander"), "implements javax.swing.text.TabExpander");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getPreferredSpan"), "method getPreferredSpan");
        check(names.contains("paint"), "method paint");
        check(names.contains("modelToView"), "method modelToView");
        check(names.contains("viewToModel"), "method viewToModel");
        check(names.contains("insertUpdate"), "method insertUpdate");
        check(names.contains("removeUpdate"), "method removeUpdate");
        check(names.contains("changedUpdate"), "method changedUpdate");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
