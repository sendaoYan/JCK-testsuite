/*
 * @test
 * @summary javax.swing.ToolTipManager conformance: type shape via reflection (headless-safe).
 * @run main ToolTipManagerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class ToolTipManagerTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.ToolTipManager", false, ToolTipManagerTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.awt.event.MouseAdapter"), "superclass");
        check(hasInterface(c, "java.awt.event.MouseMotionListener"), "implements java.awt.event.MouseMotionListener");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("setEnabled"), "method setEnabled");
        check(names.contains("isEnabled"), "method isEnabled");
        check(names.contains("setLightWeightPopupEnabled"), "method setLightWeightPopupEnabled");
        check(names.contains("isLightWeightPopupEnabled"), "method isLightWeightPopupEnabled");
        check(names.contains("setInitialDelay"), "method setInitialDelay");
        check(names.contains("getInitialDelay"), "method getInitialDelay");
        check(names.contains("setDismissDelay"), "method setDismissDelay");
        check(names.contains("getDismissDelay"), "method getDismissDelay");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
