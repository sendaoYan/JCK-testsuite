/*
 * @test
 * @summary javax.swing.Spring conformance: type shape via reflection (headless-safe).
 * @run main SpringTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class SpringTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.Spring", false, SpringTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getMinimumValue"), "method getMinimumValue");
        check(names.contains("getPreferredValue"), "method getPreferredValue");
        check(names.contains("getMaximumValue"), "method getMaximumValue");
        check(names.contains("getValue"), "method getValue");
        check(names.contains("setValue"), "method setValue");
        check(names.contains("constant"), "method constant");
        check(names.contains("constant"), "method constant");
        check(names.contains("minus"), "method minus");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
