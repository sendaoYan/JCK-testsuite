/*
 * @test
 * @summary javax.swing.RootPaneContainer conformance: type shape via reflection (headless-safe).
 * @run main RootPaneContainerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class RootPaneContainerTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.RootPaneContainer", false, RootPaneContainerTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface(), "is interface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "interface has no superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getRootPane"), "method getRootPane");
        check(names.contains("setContentPane"), "method setContentPane");
        check(names.contains("getContentPane"), "method getContentPane");
        check(names.contains("setLayeredPane"), "method setLayeredPane");
        check(names.contains("getLayeredPane"), "method getLayeredPane");
        check(names.contains("setGlassPane"), "method setGlassPane");
        check(names.contains("getGlassPane"), "method getGlassPane");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
