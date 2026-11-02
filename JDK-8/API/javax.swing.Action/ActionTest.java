/*
 * @test
 * @summary javax.swing.Action conformance: type shape via reflection (headless-safe).
 * @run main ActionTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class ActionTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.Action", false, ActionTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface(), "is interface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(hasInterface(c, "java.awt.event.ActionListener"), "implements java.awt.event.ActionListener");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getValue"), "method getValue");
        check(names.contains("putValue"), "method putValue");
        check(names.contains("setEnabled"), "method setEnabled");
        check(names.contains("isEnabled"), "method isEnabled");
        check(names.contains("addPropertyChangeListener"), "method addPropertyChangeListener");
        check(names.contains("removePropertyChangeListener"), "method removePropertyChangeListener");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
