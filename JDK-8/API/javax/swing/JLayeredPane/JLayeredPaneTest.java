/*
 * @test
 * @summary javax.swing.JLayeredPane conformance: type shape via reflection (headless-safe).
 * @run main JLayeredPaneTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class JLayeredPaneTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.JLayeredPane", false, JLayeredPaneTest.class.getClassLoader());
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
        check(names.contains("remove"), "method remove");
        check(names.contains("removeAll"), "method removeAll");
        check(names.contains("isOptimizedDrawingEnabled"), "method isOptimizedDrawingEnabled");
        check(names.contains("putLayer"), "method putLayer");
        check(names.contains("getLayer"), "method getLayer");
        check(names.contains("getLayeredPaneAbove"), "method getLayeredPaneAbove");
        check(names.contains("setLayer"), "method setLayer");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
