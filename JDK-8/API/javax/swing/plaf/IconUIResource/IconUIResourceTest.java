/*
 * @test
 * @summary javax.swing.plaf.IconUIResource conformance: type shape via reflection (headless-safe).
 * @run main IconUIResourceTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class IconUIResourceTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.plaf.IconUIResource", false, IconUIResourceTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(hasInterface(c, "javax.swing.Icon"), "implements javax.swing.Icon");
        check(hasInterface(c, "javax.swing.plaf.UIResource"), "implements javax.swing.plaf.UIResource");
        check(hasInterface(c, "java.io.Serializable"), "implements java.io.Serializable");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("paintIcon"), "method paintIcon");
        check(names.contains("getIconWidth"), "method getIconWidth");
        check(names.contains("getIconHeight"), "method getIconHeight");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
