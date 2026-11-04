/*
 * @test
 * @summary javax.swing.Box conformance: type shape via reflection (headless-safe).
 * @run main BoxTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class BoxTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.Box", false, BoxTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.JComponent"), "superclass");
        check(hasInterface(c, "javax.accessibility.Accessible"), "implements javax.accessibility.Accessible");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("createHorizontalBox"), "method createHorizontalBox");
        check(names.contains("createVerticalBox"), "method createVerticalBox");
        check(names.contains("createRigidArea"), "method createRigidArea");
        check(names.contains("createHorizontalStrut"), "method createHorizontalStrut");
        check(names.contains("createVerticalStrut"), "method createVerticalStrut");
        check(names.contains("createGlue"), "method createGlue");
        check(names.contains("createHorizontalGlue"), "method createHorizontalGlue");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
