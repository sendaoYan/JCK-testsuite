/*
 * @test
 * @summary javax.swing.plaf.basic.BasicOptionPaneUI.ButtonAreaLayout conformance: type shape via reflection (headless-safe).
 * @run main ButtonAreaLayoutTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class ButtonAreaLayoutTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.plaf.basic.BasicOptionPaneUI$ButtonAreaLayout", false, ButtonAreaLayoutTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(hasInterface(c, "java.awt.LayoutManager"), "implements java.awt.LayoutManager");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("setSyncAllWidths"), "method setSyncAllWidths");
        check(names.contains("getSyncAllWidths"), "method getSyncAllWidths");
        check(names.contains("setPadding"), "method setPadding");
        check(names.contains("getPadding"), "method getPadding");
        check(names.contains("setCentersChildren"), "method setCentersChildren");
        check(names.contains("getCentersChildren"), "method getCentersChildren");
        check(names.contains("addLayoutComponent"), "method addLayoutComponent");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
