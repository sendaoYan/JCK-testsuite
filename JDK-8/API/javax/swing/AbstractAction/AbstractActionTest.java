/*
 * @test
 * @summary javax.swing.AbstractAction conformance: type shape via reflection (headless-safe).
 * @run main AbstractActionTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class AbstractActionTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.AbstractAction", false, AbstractActionTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(hasInterface(c, "javax.swing.Action"), "implements javax.swing.Action");
        check(hasInterface(c, "java.lang.Cloneable"), "implements java.lang.Cloneable");
        check(hasInterface(c, "java.io.Serializable"), "implements java.io.Serializable");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getValue"), "method getValue");
        check(names.contains("putValue"), "method putValue");
        check(names.contains("isEnabled"), "method isEnabled");
        check(names.contains("setEnabled"), "method setEnabled");
        check(names.contains("getKeys"), "method getKeys");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
