/*
 * @test
 * @summary javax.swing.LookAndFeel conformance: type shape via reflection (headless-safe).
 * @run main LookAndFeelTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class LookAndFeelTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.LookAndFeel", false, LookAndFeelTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("installColors"), "method installColors");
        check(names.contains("installColorsAndFont"), "method installColorsAndFont");
        check(names.contains("installBorder"), "method installBorder");
        check(names.contains("uninstallBorder"), "method uninstallBorder");
        check(names.contains("installProperty"), "method installProperty");
        check(names.contains("makeKeyBindings"), "method makeKeyBindings");
        check(names.contains("makeInputMap"), "method makeInputMap");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
