/*
 * @test
 * @summary javax.swing.text.Utilities conformance: type shape via reflection (headless-safe).
 * @run main UtilitiesTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class UtilitiesTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.text.Utilities", false, UtilitiesTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("drawTabbedText"), "method drawTabbedText");
        check(names.contains("getTabbedTextWidth"), "method getTabbedTextWidth");
        check(names.contains("getTabbedTextOffset"), "method getTabbedTextOffset");
        check(names.contains("getTabbedTextOffset"), "method getTabbedTextOffset");
        check(names.contains("getBreakLocation"), "method getBreakLocation");
        check(names.contains("getRowStart"), "method getRowStart");
        check(names.contains("getRowEnd"), "method getRowEnd");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
