/*
 * @test
 * @summary javax.swing.plaf.ComponentUI conformance: type shape via reflection (headless-safe).
 * @run main ComponentUITest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class ComponentUITest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.plaf.ComponentUI", false, ComponentUITest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("installUI"), "method installUI");
        check(names.contains("uninstallUI"), "method uninstallUI");
        check(names.contains("paint"), "method paint");
        check(names.contains("update"), "method update");
        check(names.contains("getPreferredSize"), "method getPreferredSize");
        check(names.contains("getMinimumSize"), "method getMinimumSize");
        check(names.contains("getMaximumSize"), "method getMaximumSize");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
