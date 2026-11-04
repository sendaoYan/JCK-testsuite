/*
 * @test
 * @summary javax.swing.plaf.basic.BasicButtonListener conformance: type shape via reflection (headless-safe).
 * @run main BasicButtonListenerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class BasicButtonListenerTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.plaf.basic.BasicButtonListener", false, BasicButtonListenerTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(hasInterface(c, "java.awt.event.MouseListener"), "implements java.awt.event.MouseListener");
        check(hasInterface(c, "java.awt.event.MouseMotionListener"), "implements java.awt.event.MouseMotionListener");
        check(hasInterface(c, "java.awt.event.FocusListener"), "implements java.awt.event.FocusListener");
        check(hasInterface(c, "javax.swing.event.ChangeListener"), "implements javax.swing.event.ChangeListener");
        check(hasInterface(c, "java.beans.PropertyChangeListener"), "implements java.beans.PropertyChangeListener");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("propertyChange"), "method propertyChange");
        check(names.contains("installKeyboardActions"), "method installKeyboardActions");
        check(names.contains("uninstallKeyboardActions"), "method uninstallKeyboardActions");
        check(names.contains("stateChanged"), "method stateChanged");
        check(names.contains("focusGained"), "method focusGained");
        check(names.contains("focusLost"), "method focusLost");
        check(names.contains("mouseMoved"), "method mouseMoved");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
