/*
 * @test
 * @summary javax.swing.plaf.basic.BasicIconFactory conformance: type shape via reflection (headless-safe).
 * @run main BasicIconFactoryTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class BasicIconFactoryTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.plaf.basic.BasicIconFactory", false, BasicIconFactoryTest.class.getClassLoader());
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
        check(names.contains("getMenuItemCheckIcon"), "method getMenuItemCheckIcon");
        check(names.contains("getMenuItemArrowIcon"), "method getMenuItemArrowIcon");
        check(names.contains("getMenuArrowIcon"), "method getMenuArrowIcon");
        check(names.contains("getCheckBoxIcon"), "method getCheckBoxIcon");
        check(names.contains("getRadioButtonIcon"), "method getRadioButtonIcon");
        check(names.contains("getCheckBoxMenuItemIcon"), "method getCheckBoxMenuItemIcon");
        check(names.contains("getRadioButtonMenuItemIcon"), "method getRadioButtonMenuItemIcon");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
