/*
 * @test
 * @summary com.sun.java.swing.plaf.motif.MotifInternalFrameTitlePane conformance: type shape via reflection (headless-safe).
 * @run main MotifInternalFrameTitlePaneTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class MotifInternalFrameTitlePaneTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.java.swing.plaf.motif.MotifInternalFrameTitlePane", false, MotifInternalFrameTitlePaneTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.getName().equals("com.sun.java.swing.plaf.motif.MotifInternalFrameTitlePane"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.plaf.basic.BasicInternalFrameTitlePane"), "superclass");
        check(hasInterface(c, "java.awt.LayoutManager"), "implements java.awt.LayoutManager");
        check(hasInterface(c, "java.awt.event.ActionListener"), "implements java.awt.event.ActionListener");
        check(hasInterface(c, "java.beans.PropertyChangeListener"), "implements java.beans.PropertyChangeListener");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) names.add(m.getName());
        check(names.contains("installDefaults"), "method installDefaults");
        check(names.contains("uninstallListeners"), "method uninstallListeners");
        check(names.contains("createPropertyChangeListener"), "method createPropertyChangeListener");
        check(names.contains("createLayout"), "method createLayout");
        check(names.contains("assembleSystemMenu"), "method assembleSystemMenu");
        check(names.contains("createButtons"), "method createButtons");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
