/*
 * @test
 * @summary javax.swing.CellRendererPane conformance: type shape via reflection (headless-safe).
 * @run main CellRendererPaneTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class CellRendererPaneTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.CellRendererPane", false, CellRendererPaneTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.awt.Container"), "superclass");
        check(hasInterface(c, "javax.accessibility.Accessible"), "implements javax.accessibility.Accessible");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("invalidate"), "method invalidate");
        check(names.contains("paint"), "method paint");
        check(names.contains("update"), "method update");
        check(names.contains("paintComponent"), "method paintComponent");
        check(names.contains("paintComponent"), "method paintComponent");
        check(names.contains("paintComponent"), "method paintComponent");
        check(names.contains("getAccessibleContext"), "method getAccessibleContext");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
