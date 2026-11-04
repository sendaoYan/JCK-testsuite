/*
 * @test
 * @summary javax.swing.UIManager conformance: type shape via reflection (headless-safe).
 * @run main UIManagerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class UIManagerTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.UIManager", false, UIManagerTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(hasInterface(c, "java.io.Serializable"), "implements java.io.Serializable");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getInstalledLookAndFeels"), "method getInstalledLookAndFeels");
        check(names.contains("setInstalledLookAndFeels"), "method setInstalledLookAndFeels");
        check(names.contains("installLookAndFeel"), "method installLookAndFeel");
        check(names.contains("installLookAndFeel"), "method installLookAndFeel");
        check(names.contains("getLookAndFeel"), "method getLookAndFeel");
        check(names.contains("setLookAndFeel"), "method setLookAndFeel");
        check(names.contains("setLookAndFeel"), "method setLookAndFeel");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
