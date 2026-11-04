/*
 * @test
 * @summary javax.swing.JOptionPane conformance: type shape via reflection (headless-safe).
 * @run main JOptionPaneTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class JOptionPaneTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.JOptionPane", false, JOptionPaneTest.class.getClassLoader());
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
        check(names.contains("showInputDialog"), "method showInputDialog");
        check(names.contains("showInputDialog"), "method showInputDialog");
        check(names.contains("showInputDialog"), "method showInputDialog");
        check(names.contains("showInputDialog"), "method showInputDialog");
        check(names.contains("showInputDialog"), "method showInputDialog");
        check(names.contains("showInputDialog"), "method showInputDialog");
        check(names.contains("showMessageDialog"), "method showMessageDialog");
        check(names.contains("showMessageDialog"), "method showMessageDialog");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
